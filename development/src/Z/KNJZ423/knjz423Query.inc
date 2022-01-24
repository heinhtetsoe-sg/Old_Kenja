<?php

require_once('for_php7.php');

class knjz423Query extends Query {
    //エラーＤＢへの追加
    function insertQueryErr(&$db, $record_no, $check_error) {
        $data1 = array();
        $data1["PROGRAMID"][TEXT]   = PROGRAMID;
        $data1["MSGROW"][NUMBER]    = $record_no;
        $data1["MSGREMARK"][TEXT]   = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");
        $db->query($query);
    }

    //マスタの追加（ＣＳＶデータより読込）
    function insertQueryCsv($model, $data_arr) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //大、中、小分類マスタを削除
        if ($model->field["TAISYOU"] == "1") {
            $query  = " DELETE FROM INDUSTRY_L_MST ";
            $db->query($query);
            $query  = " DELETE FROM INDUSTRY_M_MST ";
            $db->query($query);
            $query  = " DELETE FROM INDUSTRY_S_MST ";
            $db->query($query);
        } else {
            $query  = " DELETE FROM JOBTYPE_L_MST ";
            $db->query($query);
            $query  = " DELETE FROM JOBTYPE_M_MST ";
            $db->query($query);
            $query  = " DELETE FROM JOBTYPE_S_MST ";
            $db->query($query);
        }

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            //データセット
            if ($model->field["TAISYOU"] == "1") {
                if ($data_arr[$i]["INDUSTRY_DCD"] == "0000") {
                    //中分類マスタ
                    if ($data_arr[$i]["INDUSTRY_SCD"] == "000") {
                        $data = array();
                        $setTable = "INDUSTRY_M_MST";

                        $data["INDUSTRY_LCD"][TEXT]             = $data_arr[$i]["INDUSTRY_LCD"];
                        $data["INDUSTRY_MCD"][TEXT]             = $data_arr[$i]["INDUSTRY_MCD"];
                        $data["INDUSTRY_MNAME"][TEXT]           = $data_arr[$i]["INDUSTRY_LMSNAME"];
                        //大分類マスタ
                        if ($data_arr[$i]["INDUSTRY_MCD"] == "00") {
                            $data = array();
                            $setTable = "INDUSTRY_L_MST";

                            $data["INDUSTRY_LCD"][TEXT]             = $data_arr[$i]["INDUSTRY_LCD"];
                            $data["INDUSTRY_LNAME"][TEXT]           = $data_arr[$i]["INDUSTRY_LMSNAME"];
                        }
                    } else {
                        //小分類マスタ
                        $data = array();
                        $setTable = "INDUSTRY_S_MST";

                        $data["INDUSTRY_LCD"][TEXT]             = $data_arr[$i]["INDUSTRY_LCD"];
                        $data["INDUSTRY_MCD"][TEXT]             = $data_arr[$i]["INDUSTRY_MCD"];
                        $data["INDUSTRY_SCD"][TEXT]             = $data_arr[$i]["INDUSTRY_SCD"];
                        $data["INDUSTRY_SNAME"][TEXT]           = $data_arr[$i]["INDUSTRY_LMSNAME"];
                    }
                    $data["REGISTERCD"][TEXT]               = STAFFCD;
                    $data["UPDATED"][NUMBER]                = "SYSDATE()";

                    $query = Query::insertSQL($data, $setTable);

                    $db->query($query);
                    $cnt++;
                }
            } else {
                //中分類マスタ
                if ($data_arr[$i]["JOBTYPE_SCD"] == "000") {
                    $data = array();
                    $setTable = "JOBTYPE_M_MST";

                    $data["JOBTYPE_LCD"][TEXT]             = $data_arr[$i]["JOBTYPE_LCD"];
                    $data["JOBTYPE_MCD"][TEXT]             = $data_arr[$i]["JOBTYPE_MCD"];
                    $data["JOBTYPE_MNAME"][TEXT]           = $data_arr[$i]["JOBTYPE_LMSNAME"];
                    //大分類マスタ
                    if ($data_arr[$i]["JOBTYPE_MCD"] == "00") {
                        $data = array();
                        $setTable = "JOBTYPE_L_MST";

                        $data["JOBTYPE_LCD"][TEXT]             = $data_arr[$i]["JOBTYPE_LCD"];
                        $data["JOBTYPE_LNAME"][TEXT]           = $data_arr[$i]["JOBTYPE_LMSNAME"];
                    }
                } else {
                    //小分類マスタ
                    $data = array();
                    $setTable = "JOBTYPE_S_MST";

                    $data["JOBTYPE_LCD"][TEXT]             = $data_arr[$i]["JOBTYPE_LCD"];
                    $data["JOBTYPE_MCD"][TEXT]             = $data_arr[$i]["JOBTYPE_MCD"];
                    $data["JOBTYPE_SCD"][TEXT]             = $data_arr[$i]["JOBTYPE_SCD"];
                    $data["JOBTYPE_SNAME"][TEXT]           = $data_arr[$i]["JOBTYPE_LMSNAME"];
                }
                $data["REGISTERCD"][TEXT]               = STAFFCD;
                $data["UPDATED"][NUMBER]                = "SYSDATE()";

                $query = Query::insertSQL($data, $setTable);

                $db->query($query);
                $cnt++;
            }
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }
}
?>
