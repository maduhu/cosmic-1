package com.cloud.api.command.admin.usage;

import com.cloud.api.APICommand;
import com.cloud.api.ApiConstants;
import com.cloud.api.BaseListCmd;
import com.cloud.api.Parameter;
import com.cloud.api.response.AccountResponse;
import com.cloud.api.response.DomainResponse;
import com.cloud.api.response.ListResponse;
import com.cloud.api.response.ProjectResponse;
import com.cloud.api.response.UsageRecordResponse;
import com.cloud.usage.Usage;
import com.cloud.utils.Pair;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "listUsageRecords", description = "Lists usage records for accounts", responseObject = UsageRecordResponse.class,
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class GetUsageRecordsCmd extends BaseListCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(GetUsageRecordsCmd.class.getName());

    private static final String s_name = "listusagerecordsresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = ApiConstants.ACCOUNT, type = CommandType.STRING, description = "List usage records for the specified user.")
    private String accountName;

    @Parameter(name = ApiConstants.DOMAIN_ID, type = CommandType.UUID, entityType = DomainResponse.class, description = "List usage records for the specified domain.")
    private Long domainId;

    @Parameter(name = ApiConstants.END_DATE,
            type = CommandType.DATE,
            required = true,
            description = "End date range for usage record query (use format \"yyyy-MM-dd\" or the new format \"yyyy-MM-dd HH:mm:ss\", e.g. startDate=2015-01-01 or " +
                    "startdate=2015-01-01 10:30:00).")
    private Date endDate;

    @Parameter(name = ApiConstants.START_DATE,
            type = CommandType.DATE,
            required = true,
            description = "Start date range for usage record query (use format \"yyyy-MM-dd\" or the new format \"yyyy-MM-dd HH:mm:ss\", e.g. startDate=2015-01-01 or " +
                    "startdate=2015-01-01 11:00:00).")
    private Date startDate;

    @Parameter(name = ApiConstants.ACCOUNT_ID, type = CommandType.UUID, entityType = AccountResponse.class, description = "List usage records for the specified account")
    private Long accountId;

    @Parameter(name = ApiConstants.PROJECT_ID, type = CommandType.UUID, entityType = ProjectResponse.class, description = "List usage records for specified project")
    private Long projectId;

    @Parameter(name = ApiConstants.TYPE, type = CommandType.LONG, description = "List usage records for the specified usage type")
    private Long usageType;

    @Parameter(name = ApiConstants.USAGE_ID, type = CommandType.STRING, description = "List usage records for the specified usage UUID. Can be used only together with TYPE " +
            "parameter.")
    private String usageId;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(final String accountName) {
        this.accountName = accountName;
    }

    public Long getDomainId() {
        return domainId;
    }

    public void setDomainId(final Long domainId) {
        this.domainId = domainId;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(final Date endDate) {
        this.endDate = endDate == null ? null : new Date(endDate.getTime());
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(final Date startDate) {
        this.startDate = startDate == null ? null : new Date(startDate.getTime());
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(final Long accountId) {
        this.accountId = accountId;
    }

    public Long getUsageType() {
        return usageType;
    }

    public Long getProjectId() {
        return projectId;
    }

    public String getUsageId() {
        return usageId;
    }

    public void setUsageId(final String usageId) {
        this.usageId = usageId;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public void execute() {
        final Pair<List<? extends Usage>, Integer> usageRecords = _usageService.getUsageRecords(this);
        final ListResponse<UsageRecordResponse> response = new ListResponse<>();
        final List<UsageRecordResponse> usageResponses = new ArrayList<>();
        if (usageRecords != null) {
            for (final Usage usageRecord : usageRecords.first()) {
                final UsageRecordResponse usageResponse = _responseGenerator.createUsageResponse(usageRecord);
                usageResponse.setObjectName("usagerecord");
                usageResponses.add(usageResponse);
            }
            response.setResponses(usageResponses, usageRecords.second());
        }

        response.setResponseName(getCommandName());
        this.setResponseObject(response);
    }

    @Override
    public String getCommandName() {
        return s_name;
    }
}
