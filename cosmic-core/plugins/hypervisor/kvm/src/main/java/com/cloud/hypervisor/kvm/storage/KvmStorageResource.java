package com.cloud.hypervisor.kvm.storage;

import com.cloud.agent.api.Answer;
import com.cloud.hypervisor.kvm.resource.LibvirtComputingResource;
import com.cloud.storage.command.AttachCommand;
import com.cloud.storage.command.AttachPrimaryDataStoreCmd;
import com.cloud.storage.command.CopyCommand;
import com.cloud.storage.command.CreateObjectCommand;
import com.cloud.storage.command.CreatePrimaryDataStoreCmd;
import com.cloud.storage.command.DeleteCommand;
import com.cloud.storage.command.DettachCommand;
import com.cloud.storage.command.StorageSubSystemCommand;

public class KvmStorageResource {

    private final LibvirtComputingResource resource;

    public KvmStorageResource(final LibvirtComputingResource resource) {
        this.resource = resource;
    }

    public Answer handleStorageCommands(final StorageSubSystemCommand command) {
        if (command instanceof CopyCommand) {
            return this.execute((CopyCommand) command);
        } else if (command instanceof AttachPrimaryDataStoreCmd) {
            return this.execute((AttachPrimaryDataStoreCmd) command);
        } else if (command instanceof CreatePrimaryDataStoreCmd) {
            return execute((CreatePrimaryDataStoreCmd) command);
        } else if (command instanceof CreateObjectCommand) {
            return execute((CreateObjectCommand) command);
        } else if (command instanceof DeleteCommand) {
            return execute((DeleteCommand) command);
        } else if (command instanceof AttachCommand) {
            return execute((AttachCommand) command);
        } else if (command instanceof DettachCommand) {
            return execute((DettachCommand) command);
        }
        return new Answer(command, false, "not implemented yet");
    }

    protected Answer execute(final CopyCommand cmd) {
        return new Answer(cmd, false, "not implemented yet");
    }

    protected Answer execute(final AttachPrimaryDataStoreCmd cmd) {
        return new Answer(cmd, false, "not implemented yet");
    }

    protected Answer execute(final CreatePrimaryDataStoreCmd cmd) {
        return new Answer(cmd, false, "not implemented yet");
    }

    protected Answer execute(final CreateObjectCommand cmd) {
        return new Answer(cmd, false, "not implemented yet");
    }

    protected Answer execute(final DeleteCommand cmd) {
        return new Answer(cmd, false, "not implemented yet");
    }

    protected Answer execute(final AttachCommand cmd) {
        return new Answer(cmd, false, "not implemented yet");
    }

    protected Answer execute(final DettachCommand cmd) {
        return new Answer(cmd, false, "not implemented yet");
    }
}
