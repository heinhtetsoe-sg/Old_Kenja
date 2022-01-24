<?php

require_once('for_php7.php');

class knjxsearch9_job_ssQuery extends Query
{
    public function searchStudent($fields)
    {
        $query  = "SELECT * ";
        $query .= "  FROM company_mst ";
        if (strlen($fields["company_name"]) && strlen($fields["shushoku_addr"])) {
            $query .= " WHERE company_name  like '%".$fields["company_name"]."%'";
            $query .= "   AND shushoku_addr like '%".$fields["shushoku_addr"]."%'";
        } else {
            if (strlen($fields["company_name"])) {
                $query .= " WHERE company_name  like '%".$fields["company_name"]."%'";
            }
            if (strlen($fields["shushoku_addr"])) {
                $query .= " WHERE shushoku_addr like '%".$fields["shushoku_addr"]."%'";
            }
        }
        $query .= " ORDER BY company_cd";

        return $query;
    }
}
