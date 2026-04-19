package com.mockr.runnr.dto;

import java.util.UUID;

/**
 * ProjectContext DTO - Holds project details extracted from incoming request.
 * 
 * Contains project identification and metadata resolved from the context path
 * in the request URL.
 */
public class ProjectContext {

    private final UUID projectId;
    private final String contextPath;
    private final String name;
    private final String description;

    private ProjectContext(Builder builder) {
        this.projectId = builder.projectId;
        this.contextPath = builder.contextPath;
        this.name = builder.name;
        this.description = builder.description;
    }

    public UUID getProjectId() {
        return projectId;
    }

    public String getContextPath() {
        return contextPath;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "ProjectContext{" +
                "projectId=" + projectId +
                ", contextPath='" + contextPath + '\'' +
                ", name='" + name + '\'' +
                '}';
    }

    /**
     * Builder for ProjectContext.
     */
    public static class Builder {
        private UUID projectId;
        private String contextPath;
        private String name;
        private String description;

        public Builder projectId(UUID projectId) {
            this.projectId = projectId;
            return this;
        }

        public Builder contextPath(String contextPath) {
            this.contextPath = contextPath;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public ProjectContext build() {
            return new ProjectContext(this);
        }
    }
}
