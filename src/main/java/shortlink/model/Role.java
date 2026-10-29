package shortlink.model;

import java.time.OffsetDateTime;

/*
* Role Model
* */
public class Role {
    /*
    * Id
    * */
    private Integer id;

    /*
    * Role name
    * */
    private String roleName;

    /*
    * Role description
    * */
    private String roleDescription;

    /*
    * Created at
    * */
    private OffsetDateTime createdAt;


    /*
    * Constructor
    * */
    public Role() {}

    /*
    * Parameter Constructor
    * */
    public Role(Integer id, String roleName, String roleDescription, OffsetDateTime createdAt) {
        this.id = id;
        this.roleName = roleName;
        this.roleDescription = roleDescription;
        this.createdAt = createdAt;
    }

    /*
     * Get id
     * */
    public Integer getId() {
        return id;
    }

    /*
     * Set id
     * */
    public void setId(Integer id) {
        this.id = id;
    }

    /*
     * Get role name
     * */
    public String getRoleName() {
        return roleName;
    }

    /*
     * Set role name
     * */
    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    /*
     * Get role description
     * */
    public String getRoleDescription() {
        return roleDescription;
    }

    /*
    * Set role description
    * */
    public void setRoleDescription(String roleDescription) {
        this.roleDescription = roleDescription;
    }

    /*
     * Get created at
     * */
    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    /*
    * Set created at
    * */
    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
