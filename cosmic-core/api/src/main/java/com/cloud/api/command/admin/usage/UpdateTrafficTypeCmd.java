package com.cloud.api.command.admin.usage;

import com.cloud.api.APICommand;
import com.cloud.api.ApiCommandJobType;
import com.cloud.api.ApiConstants;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.BaseAsyncCmd;
import com.cloud.api.Parameter;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.TrafficTypeResponse;
import com.cloud.event.EventTypes;
import com.cloud.network.PhysicalNetworkTrafficType;
import com.cloud.user.Account;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "updateTrafficType", description = "Updates traffic type of a physical network", responseObject = TrafficTypeResponse.class, since = "3.0.0",
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class UpdateTrafficTypeCmd extends BaseAsyncCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(UpdateTrafficTypeCmd.class.getName());

    private static final String s_name = "updatetraffictyperesponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = ApiConstants.ID, type = CommandType.UUID, entityType = TrafficTypeResponse.class, required = true, description = "traffic type id")
    private Long id;

    @Parameter(name = ApiConstants.XENSERVER_NETWORK_LABEL,
            type = CommandType.STRING,
            description = "The network name label of the physical device dedicated to this traffic on a XenServer host")
    private String xenLabel;

    @Parameter(name = ApiConstants.KVM_NETWORK_LABEL,
            type = CommandType.STRING,
            description = "The network name label of the physical device dedicated to this traffic on a KVM host")
    private String kvmLabel;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    @Override
    public void execute() {
        final PhysicalNetworkTrafficType result = _networkService.updatePhysicalNetworkTrafficType(getId(), getXenLabel(), getKvmLabel());
        if (result != null) {
            final TrafficTypeResponse response = _responseGenerator.createTrafficTypeResponse(result);
            response.setResponseName(getCommandName());
            setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to update traffic type");
        }
    }

    public Long getId() {
        return id;
    }

    public String getXenLabel() {
        return xenLabel;
    }

    public String getKvmLabel() {
        return kvmLabel;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public long getEntityOwnerId() {
        return Account.ACCOUNT_ID_SYSTEM;
    }

    @Override
    public String getEventType() {
        return EventTypes.EVENT_TRAFFIC_TYPE_UPDATE;
    }

    @Override
    public String getEventDescription() {
        return "Updating Traffic Type: " + getId();
    }

    @Override
    public ApiCommandJobType getInstanceType() {
        return ApiCommandJobType.TrafficType;
    }
}
