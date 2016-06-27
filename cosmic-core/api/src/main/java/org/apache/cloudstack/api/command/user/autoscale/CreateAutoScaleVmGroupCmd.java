package org.apache.cloudstack.api.command.user.autoscale;

import com.cloud.event.EventTypes;
import com.cloud.exception.InvalidParameterValueException;
import com.cloud.exception.ResourceAllocationException;
import com.cloud.network.as.AutoScaleVmGroup;
import com.cloud.network.rules.LoadBalancer;
import org.apache.cloudstack.acl.RoleType;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiCommandJobType;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.ApiErrorCode;
import org.apache.cloudstack.api.BaseAsyncCreateCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.response.AutoScalePolicyResponse;
import org.apache.cloudstack.api.response.AutoScaleVmGroupResponse;
import org.apache.cloudstack.api.response.AutoScaleVmProfileResponse;
import org.apache.cloudstack.api.response.FirewallRuleResponse;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "createAutoScaleVmGroup",
        description = "Creates and automatically starts a virtual machine based on a service offering, disk offering, and template.",
        responseObject = AutoScaleVmGroupResponse.class, entityType = {AutoScaleVmGroup.class},
        requestHasSensitiveInfo = false,
        responseHasSensitiveInfo = false)
public class CreateAutoScaleVmGroupCmd extends BaseAsyncCreateCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(CreateAutoScaleVmGroupCmd.class.getName());

    private static final String s_name = "autoscalevmgroupresponse";

    // ///////////////////////////////////////////////////
    // ////////////// API parameters /////////////////////
    // ///////////////////////////////////////////////////

    @Parameter(name = ApiConstants.LBID,
            type = CommandType.UUID,
            entityType = FirewallRuleResponse.class,
            required = true,
            description = "the ID of the load balancer rule")
    private long lbRuleId;

    @Parameter(name = ApiConstants.MIN_MEMBERS,
            type = CommandType.INTEGER,
            required = true,
            description = "the minimum number of members in the vmgroup, the number of instances in the vm group will be equal to or more than this number.")
    private int minMembers;

    @Parameter(name = ApiConstants.MAX_MEMBERS,
            type = CommandType.INTEGER,
            required = true,
            description = "the maximum number of members in the vmgroup, The number of instances in the vm group will be equal to or less than this number.")
    private int maxMembers;

    @Parameter(name = ApiConstants.INTERVAL, type = CommandType.INTEGER, description = "the frequency at which the conditions have to be evaluated")
    private Integer interval;

    @Parameter(name = ApiConstants.SCALEUP_POLICY_IDS,
            type = CommandType.LIST,
            collectionType = CommandType.UUID,
            entityType = AutoScalePolicyResponse.class,
            required = true,
            description = "list of scaleup autoscale policies")
    private List<Long> scaleUpPolicyIds;

    @Parameter(name = ApiConstants.SCALEDOWN_POLICY_IDS,
            type = CommandType.LIST,
            collectionType = CommandType.UUID,
            entityType = AutoScalePolicyResponse.class,
            required = true,
            description = "list of scaledown autoscale policies")
    private List<Long> scaleDownPolicyIds;

    @Parameter(name = ApiConstants.VMPROFILE_ID,
            type = CommandType.UUID,
            entityType = AutoScaleVmProfileResponse.class,
            required = true,
            description = "the autoscale profile that contains information about the vms in the vm group.")
    private long profileId;

    @Parameter(name = ApiConstants.FOR_DISPLAY, type = CommandType.BOOLEAN, description = "an optional field, whether to the display the group to the end user or not", since =
            "4.4", authorized = {RoleType.Admin})
    private Boolean display;

    // ///////////////////////////////////////////////////
    // ///////////////// Accessors ///////////////////////
    // ///////////////////////////////////////////////////

    public static String getResultObjectName() {
        return "autoscalevmgroup";
    }

    public int getMinMembers() {
        return minMembers;
    }

    public int getMaxMembers() {
        return maxMembers;
    }

    public Integer getInterval() {
        return interval;
    }

    public long getProfileId() {
        return profileId;
    }

    public List<Long> getScaleUpPolicyIds() {
        return scaleUpPolicyIds;
    }

    public List<Long> getScaleDownPolicyIds() {
        return scaleDownPolicyIds;
    }

    // ///////////////////////////////////////////////////
    // ///////////// API Implementation///////////////////
    // ///////////////////////////////////////////////////

    @Override
    public String getEventType() {
        return EventTypes.EVENT_AUTOSCALEVMGROUP_CREATE;
    }

    @Override
    public String getEventDescription() {
        return "configuring AutoScale Vm Group. Vm Group Id: " + getEntityId();
    }

    @Override
    public ApiCommandJobType getInstanceType() {
        return ApiCommandJobType.AutoScaleVmGroup;
    }

    @Deprecated
    public Boolean getDisplay() {
        return display;
    }

    @Override
    public void create() throws ResourceAllocationException {
        final AutoScaleVmGroup result = _autoScaleService.createAutoScaleVmGroup(this);
        if (result != null) {
            setEntityId(result.getId());
            setEntityUuid(result.getUuid());
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to create Autoscale Vm Group");
        }
    }

    @Override
    public String getCreateEventType() {
        return EventTypes.EVENT_AUTOSCALEVMGROUP_CREATE;
    }

    @Override
    public String getCreateEventDescription() {
        return "creating AutoScale Vm Group";
    }

    @Override
    public void execute() {
        boolean success = false;
        AutoScaleVmGroup vmGroup = null;
        try {
            success = _autoScaleService.configureAutoScaleVmGroup(this);
            if (success) {
                vmGroup = _entityMgr.findById(AutoScaleVmGroup.class, getEntityId());
                final AutoScaleVmGroupResponse responseObject = _responseGenerator.createAutoScaleVmGroupResponse(vmGroup);
                setResponseObject(responseObject);
                responseObject.setResponseName(getCommandName());
            }
        } catch (final Exception ex) {
            // TODO what will happen if Resource Layer fails in a step inbetween
            s_logger.warn("Failed to create autoscale vm group", ex);
        } finally {
            if (!success || vmGroup == null) {
                _autoScaleService.deleteAutoScaleVmGroup(getEntityId());
                throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to create Autoscale Vm Group");
            }
        }
    }

    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public long getEntityOwnerId() {
        final LoadBalancer lb = _entityMgr.findById(LoadBalancer.class, getLbRuleId());
        if (lb == null) {
            throw new InvalidParameterValueException("Unable to find loadbalancer by lbRuleId");
        }
        return lb.getAccountId();
    }

    public long getLbRuleId() {
        return lbRuleId;
    }

    public void setLbRuleId(final Long lbRuleId) {
        this.lbRuleId = lbRuleId;
    }

    @Override
    public boolean isDisplay() {
        if (display == null) {
            return true;
        } else {
            return display;
        }
    }
}
