""" BVT tests for Primary Storage
"""
# Import Local Modules
from marvin.cloudstackAPI import *
from marvin.cloudstackTestCase import *
from marvin.lib.base import *
from marvin.lib.common import *
from marvin.lib.utils import *
from nose.plugins.attrib import attr

# Import System modules

_multiprocess_shared_ = True


class TestPrimaryStorageServices(cloudstackTestCase):
    def setUp(self):

        self.apiclient = self.testClient.getApiClient()
        self.services = self.testClient.getParsedTestDataConfig()
        self.cleanup = []
        # Get Zone and pod
        self.zone = get_zone(self.apiclient, self.testClient.getZoneForTests())
        self.pod = get_pod(self.apiclient, self.zone.id)
        self.hypervisor = self.testClient.getHypervisorInfo()

        return

    def tearDown(self):
        try:
            # Clean up, terminate the created templates
            cleanup_resources(self.apiclient, self.cleanup)

        except Exception as e:
            raise Exception("Warning: Exception during cleanup : %s" % e)
        return

    @attr(tags=["advanced", "advancedns", "smoke", "basic", "sg"], required_hardware="false")
    def test_01_primary_storage_nfs(self):
        """Test primary storage pools - XEN, KVM, VMWare.
        """

        # Validate the following:
        # 1. List Clusters
        # 2. verify that the cluster is in 'Enabled' allocation state
        # 3. verify that the host is added successfully and
        #    in Up state with listHosts api response

        # Create NFS storage pools with on XEN/KVM/VMWare clusters


        clusters = list_clusters(
            self.apiclient,
            zoneid=self.zone.id
        )
        assert isinstance(clusters, list) and len(clusters) > 0
        for cluster in clusters:
            # Host should be present before adding primary storage
            list_hosts_response = list_hosts(
                self.apiclient,
                clusterid=cluster.id
            )
            self.assertEqual(
                isinstance(list_hosts_response, list),
                True,
                "Check list response returns a valid list"
            )

            self.assertNotEqual(
                len(list_hosts_response),
                0,
                "Check list Hosts in the cluster: " + cluster.name
            )

            storage = StoragePool.create(self.apiclient,
                                         self.services["nfs"],
                                         clusterid=cluster.id,
                                         zoneid=self.zone.id,
                                         podid=self.pod.id
                                         )
            self.cleanup.append(storage)

            self.debug("Created storage pool in cluster: %s" % cluster.id)

            self.assertEqual(
                storage.state,
                'Up',
                "Check primary storage state "
            )

            self.assertEqual(
                storage.type,
                'NetworkFilesystem',
                "Check storage pool type "
            )

            # Verify List Storage pool Response has newly added storage pool
            storage_pools_response = list_storage_pools(
                self.apiclient,
                id=storage.id,
            )
            self.assertEqual(
                isinstance(storage_pools_response, list),
                True,
                "Check list response returns a valid list"
            )
            self.assertNotEqual(
                len(storage_pools_response),
                0,
                "Check list Hosts response"
            )

            storage_response = storage_pools_response[0]
            self.assertEqual(
                storage_response.id,
                storage.id,
                "Check storage pool ID"
            )
            self.assertEqual(
                storage.type,
                storage_response.type,
                "Check storage pool type "
            )
            # Call cleanup for reusing primary storage
            cleanup_resources(self.apiclient, self.cleanup)
            self.cleanup = []
            return

    @attr(tags=["advanced", "advancedns", "smoke", "basic", "sg"], required_hardware="true")
    def test_01_primary_storage_iscsi(self):
        """Test primary storage pools - XEN. Not Supported for kvm,vmware
        """

        if self.hypervisor.lower() in ["kvm", "vmware"]:
            raise self.skipTest("iscsi primary storage not supported on kvm or VMWare")

        # Validate the following:
        # 1. List Clusters
        # 2. verify that the cluster is in 'Enabled' allocation state
        # 3. verify that the host is added successfully and
        #    in Up state with listHosts api response

        # Create iSCSI storage pools with on XEN/KVM clusters
        clusters = list_clusters(
            self.apiclient,
            zoneid=self.zone.id
        )
        assert isinstance(clusters, list) and len(clusters) > 0
        for cluster in clusters:
            # Host should be present before adding primary storage
            list_hosts_response = list_hosts(
                self.apiclient,
                clusterid=cluster.id
            )
            self.assertEqual(
                isinstance(list_hosts_response, list),
                True,
                "Check list response returns a valid list"
            )

            self.assertNotEqual(
                len(list_hosts_response),
                0,
                "Check list Hosts in the cluster: " + cluster.name
            )

            storage = StoragePool.create(self.apiclient,
                                         self.services["configurableData"]["iscsi"],
                                         clusterid=cluster.id,
                                         zoneid=self.zone.id,
                                         podid=self.pod.id
                                         )
            self.cleanup.append(storage)

            self.debug("Created storage pool in cluster: %s" % cluster.id)

            self.assertEqual(
                storage.state,
                'Up',
                "Check primary storage state "
            )

            self.assertEqual(
                storage.type,
                'IscsiLUN',
                "Check storage pool type "
            )

            # Verify List Storage pool Response has newly added storage pool
            storage_pools_response = list_storage_pools(
                self.apiclient,
                id=storage.id,
            )
            self.assertEqual(
                isinstance(storage_pools_response, list),
                True,
                "Check list response returns a valid list"
            )
            self.assertNotEqual(
                len(storage_pools_response),
                0,
                "Check list Hosts response"
            )

            storage_response = storage_pools_response[0]
            self.assertEqual(
                storage_response.id,
                storage.id,
                "Check storage pool ID"
            )
            self.assertEqual(
                storage.type,
                storage_response.type,
                "Check storage pool type "
            )
            # Call cleanup for reusing primary storage
            cleanup_resources(self.apiclient, self.cleanup)
            self.cleanup = []

        return
