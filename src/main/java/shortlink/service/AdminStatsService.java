package shortlink.service;

import shortlink.dto.AdminStatsResponse;

/*
* Admin stats service
* */
public interface AdminStatsService {

    /*
    * Numbers for the admin dashboard : users per status and total links
    * */
    AdminStatsResponse getStats();
}
