<?php

require_once('for_php7.php');

class knjl151kQuery extends Query
{

    //実行
    public function insertQuery(&$model, &$data_arr)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        $cnt = 0;
        //UPDATE or INSERT or DELETE
        $count = get_count($data_arr);
        for ($i = 0; $i < $count; $i++) {
            switch ($model->target) {
                case "1":  //基礎・住所・得点データ
                    $query  = " DELETE FROM ";
                    $query .= "       ENTEXAM_APPLICANTBASE_DAT ";
                    $query .= " WHERE ";
                    $query .= "     ENTEXAMYEAR = '".$data_arr[$i]["ENTEXAMYEAR"]."' ";
                    $query .= "     AND TESTDIV = '".$data_arr[$i]["TESTDIV"]."' ";
                    $query .= "     AND EXAMNO  = '".$data_arr[$i]["EXAMNO"]."' ";
                    $db->query($query);

                    $data = array();
                    $data["ENTEXAMYEAR"][TEXT]            = $data_arr[$i]["ENTEXAMYEAR"];
                    $data["TESTDIV"][TEXT]                = $data_arr[$i]["TESTDIV"];
                    $data["EXAMNO"][TEXT]                 = $data_arr[$i]["EXAMNO"];
                    $data["SHDIV"][TEXT]                  = $data_arr[$i]["SHDIV"];
                    $data["DESIREDIV"][TEXT]              = $data_arr[$i]["DESIREDIV"];
                    $data["APPLICANTDIV"][TEXT]           = $data_arr[$i]["APPLICANTDIV"];
                    $data["CLUBCD"][TEXT]                 = $data_arr[$i]["CLUBCD"];
                    $data["SPECIAL_REASON_DIV"][TEXT]     = $data_arr[$i]["SPECIAL_REASON_DIV"];
                    $data["NAME"][TEXT]                   = $data_arr[$i]["NAME"];
                    $data["NAME_KANA"][TEXT]              = $data_arr[$i]["NAME_KANA"];
                    $data["SEX"][TEXT]                    = $data_arr[$i]["SEX"];
                    $data["BIRTHDAY"][DATE]               = $data_arr[$i]["BIRTHDAY"];
                    $data["ADDRESSCD"][TEXT]              = $data_arr[$i]["ADDRESSCD"];
                    $data["TELNO"][TEXT]                  = $data_arr[$i]["TELNO"];
                    $data["LOCATIONCD"][TEXT]             = $data_arr[$i]["LOCATIONCD"];
                    $data["NATPUBPRIDIV"][TEXT]           = $data_arr[$i]["NATPUBPRIDIV"];
                    $data["FS_CD"][TEXT]                  = $data_arr[$i]["FS_CD"];
                    $data["FS_GRDYEAR"][TEXT]             = $data_arr[$i]["FS_GRDYEAR"];
                    $data["PS_CD"][TEXT]                  = $data_arr[$i]["PS_CD"];
                    $data["APPROVAL_FLG"][TEXT]           = $data_arr[$i]["APPROVAL_FLG"];
                    $data["GNAME"][TEXT]                  = $data_arr[$i]["GNAME"];
                    $data["GKANA"][TEXT]                  = $data_arr[$i]["GKANA"];
                    $data["GTELNO"][TEXT]                 = $data_arr[$i]["GTELNO"];
                    $data["A_TOTAL"][NUMBER]              = $data_arr[$i]["A_TOTAL"];
                    $data["A_AVERAGE"][NUMBER]            = $data_arr[$i]["A_AVERAGE"];
                    $data["A_TOTAL_RANK"][NUMBER]         = $data_arr[$i]["A_TOTAL_RANK"];
                    $data["A_DIV_RANK"][NUMBER]           = $data_arr[$i]["A_DIV_RANK"];
                    $data["B_TOTAL"][NUMBER]              = $data_arr[$i]["B_TOTAL"];
                    $data["B_AVERAGE"][NUMBER]            = $data_arr[$i]["B_AVERAGE"];
                    $data["B_TOTAL_RANK"][NUMBER]         = $data_arr[$i]["B_TOTAL_RANK"];
                    $data["B_DIV_RANK"][NUMBER]           = $data_arr[$i]["B_DIV_RANK"];
                    $data["JUDGEMENT"][TEXT]              = $data_arr[$i]["JUDGEMENT"];
                    $data["JUDGEMENT_GROUP_NO"][TEXT]     = $data_arr[$i]["JUDGEMENT_GROUP_NO"];
                    $data["INTERVIEW_ATTEND_FLG"][TEXT]   = $data_arr[$i]["INTERVIEW_ATTEND_FLG"];
                    $data["SCALASHIPDIV"][TEXT]           = $data_arr[$i]["SCALASHIPDIV"];
                    $data["SUC_COURSECD"][TEXT]           = $data_arr[$i]["SUC_COURSECD"];
                    $data["SUC_MAJORCD"][TEXT]            = $data_arr[$i]["SUC_MAJORCD"];
                    $data["SUC_COURSECODE"][TEXT]         = $data_arr[$i]["SUC_COURSECODE"];
                    $data["PROCEDUREDIV"][TEXT]           = $data_arr[$i]["PROCEDUREDIV"];
                    $data["ENTDIV"][TEXT]                 = $data_arr[$i]["ENTDIV"];
                    $data["FORMNO"][TEXT]                 = $data_arr[$i]["FORMNO"];
                    $data["SUCCESS_NOTICENO"][TEXT]       = $data_arr[$i]["SUCCESS_NOTICENO"];
                    $data["OLD_SUCCESS_NOTICENO"][TEXT]   = $data_arr[$i]["OLD_SUCCESS_NOTICENO"];
                    $data["FAILURE_NOTICENO"][TEXT]       = $data_arr[$i]["FAILURE_NOTICENO"];
                    $data["REGISTERCD"][TEXT]             = STAFFCD;
                    $data["UPDATED"][FUNC]                = "SYSDATE()";

                    $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DAT");
                    $db->query($query);

                    $query  = " DELETE FROM ";
                    $query .= "       ENTEXAM_APPLICANTADDR_DAT ";
                    $query .= " WHERE ";
                    $query .= "     ENTEXAMYEAR = '".$data_arr[$i]["ENTEXAMYEAR"]."' ";
                    $query .= "     AND TESTDIV = '".$data_arr[$i]["TESTDIV"]."' ";
                    $query .= "     AND EXAMNO  = '".$data_arr[$i]["EXAMNO"]."' ";
                    $db->query($query);

                    $data = array();
                    $data["ENTEXAMYEAR"][TEXT]    = $data_arr[$i]["ENTEXAMYEAR"];
                    $data["TESTDIV"][TEXT]        = $data_arr[$i]["TESTDIV"];
                    $data["EXAMNO"][TEXT]         = $data_arr[$i]["EXAMNO"];
                    $data["ZIPCD"][TEXT]          = $data_arr[$i]["ZIPCD"];
                    $data["ADDRESS"][TEXT]        = $data_arr[$i]["ADDRESS"];
                    $data["GZIPCD"][TEXT]         = $data_arr[$i]["GZIPCD"];
                    $data["GADDRESS"][TEXT]       = $data_arr[$i]["GADDRESS"];
                    $data["REGISTERCD"][TEXT]     = STAFFCD;
                    $data["UPDATED"][FUNC]        = "SYSDATE()";

                    $query = Query::insertSQL($data, "ENTEXAM_APPLICANTADDR_DAT");
                    $db->query($query);

                    $query  = " DELETE FROM ";
                    $query .= "       ENTEXAM_SCORE_DAT ";
                    $query .= " WHERE ";
                    $query .= "     ENTEXAMYEAR = '".$data_arr[$i]["ENTEXAMYEAR"]."' ";
                    $query .= "     AND TESTDIV = '".$data_arr[$i]["TESTDIV"]."' ";
                    $query .= "     AND EXAMNO  = '".$data_arr[$i]["EXAMNO"]."' ";
                    $db->query($query);

                    for ($scoreCount = 1; $scoreCount <= 5; $scoreCount++) {
                        if (!strlen(trim($data_arr[$i]["TESTSUBCLASSCD".$scoreCount]))) {
                            continue;
                        }
                        $data = array();
                        $data["ENTEXAMYEAR"][TEXT]    = $data_arr[$i]["ENTEXAMYEAR"];
                        $data["TESTDIV"][TEXT]        = $data_arr[$i]["TESTDIV"];
                        $data["EXAMNO"][TEXT]         = $data_arr[$i]["EXAMNO"];
                        $data["TESTSUBCLASSCD"][TEXT] = $data_arr[$i]["TESTSUBCLASSCD".$scoreCount];
                        $data["ATTEND_FLG"][TEXT]     = $data_arr[$i]["ATTEND_FLG".$scoreCount];
                        $data["A_SCORE"][NUMBER]      = $data_arr[$i]["A_SCORE".$scoreCount];
                        $data["A_STD_SCORE"][NUMBER]  = $data_arr[$i]["A_STD_SCORE".$scoreCount];
                        $data["A_RANK"][NUMBER]       = $data_arr[$i]["A_RANK".$scoreCount];
                        $data["B_SCORE"][NUMBER]      = $data_arr[$i]["B_SCORE".$scoreCount];
                        $data["B_STD_SCORE"][NUMBER]  = $data_arr[$i]["B_STD_SCORE".$scoreCount];
                        $data["B_RANK"][NUMBER]       = $data_arr[$i]["B_RANK".$scoreCount];
                        $data["REGISTERCD"][TEXT]     = STAFFCD;
                        $data["UPDATED"][FUNC]        = "SYSDATE()";

                        $query = Query::insertSQL($data, "ENTEXAM_SCORE_DAT");
                        $db->query($query);
                    }
                    break;

                case "2":  //事前相談データ
                    //'/' ⇒ '-'に変換
                    $data_set = str_replace("/", "-", $data_arr[$i]["CREATE_DATE"]);

                    $query  = " DELETE FROM ";
                    $query .= "       ENTEXAM_CONSULTATION_HDAT ";
                    $query .= " WHERE ";
                    $query .= "     ENTEXAMYEAR     = '".$data_arr[$i]["ENTEXAMYEAR"]."' ";
                    $query .= "     AND TESTDIV     = '".$data_arr[$i]["TESTDIV"]."' ";
                    $query .= "     AND ACCEPTNO    = '".$data_arr[$i]["ACCEPTNO"]."' ";
                    $query .= "     AND CREATE_DATE = '".$data_set."' ";
                    $db->query($query);
                    
                    $data = array();
                    $data["ENTEXAMYEAR"][TEXT]    = $data_arr[$i]["ENTEXAMYEAR"];
                    $data["TESTDIV"][TEXT]        = $data_arr[$i]["TESTDIV"];
                    $data["ACCEPTNO"][TEXT]       = $data_arr[$i]["ACCEPTNO"];
                    $data["CREATE_DATE"][DATE]    = $data_arr[$i]["CREATE_DATE"];
                    $data["EXAMNO"][TEXT]         = $data_arr[$i]["EXAMNO"];
                    $data["NAME"][TEXT]           = $data_arr[$i]["NAME"];
                    $data["NAME_KANA"][TEXT]      = $data_arr[$i]["NAME_KANA"];
                    $data["SEX"][TEXT]            = $data_arr[$i]["SEX"];
                    $data["PS_UPDATED"][DATE]     = $data_arr[$i]["PS_UPDATED"];
                    $data["PS_ACCEPTNO"][TEXT]    = $data_arr[$i]["PS_ACCEPTNO"];
                    $data["PS_CD"][TEXT]          = $data_arr[$i]["PS_CD"];
                    $data["PS_ITEM1"][NUMBER]     = $data_arr[$i]["PS_ITEM1"];
                    $data["PS_ITEM2"][NUMBER]     = $data_arr[$i]["PS_ITEM2"];
                    $data["PS_ITEM3"][NUMBER]     = $data_arr[$i]["PS_ITEM3"];
                    $data["PS_ITEM4"][NUMBER]     = $data_arr[$i]["PS_ITEM4"];
                    $data["PS_ITEM5"][NUMBER]     = $data_arr[$i]["PS_ITEM5"];
                    $data["FS_UPDATED"][DATE]     = $data_arr[$i]["FS_UPDATED"];
                    $data["FS_ACCEPTNO"][TEXT]    = $data_arr[$i]["FS_ACCEPTNO"];
                    $data["FS_CD"][TEXT]          = $data_arr[$i]["FS_CD"];
                    $data["FS_ITEM1"][NUMBER]     = $data_arr[$i]["FS_ITEM1"];
                    $data["FS_ITEM2"][NUMBER]     = $data_arr[$i]["FS_ITEM2"];
                    $data["FS_ITEM3"][NUMBER]     = $data_arr[$i]["FS_ITEM3"];
                    $data["FS_ITEM4"][NUMBER]     = $data_arr[$i]["FS_ITEM4"];
                    $data["FS_ITEM5"][NUMBER]     = $data_arr[$i]["FS_ITEM5"];
                    $data["REGISTERCD"][TEXT]     = STAFFCD;
                    $data["UPDATED"][FUNC]        = "SYSDATE()";

                    $query = Query::insertSQL($data, "ENTEXAM_CONSULTATION_HDAT");
                    $db->query($query);

                    $query  = " DELETE FROM ";
                    $query .= "       ENTEXAM_CONSULTATION_DAT ";
                    $query .= " WHERE ";
                    $query .= "     ENTEXAMYEAR  = '".$data_arr[$i]["ENTEXAMYEAR"]."' ";
                    $query .= "     AND TESTDIV  = '".$data_arr[$i]["TESTDIV"]."' ";
                    $query .= "     AND ACCEPTNO = '".$data_arr[$i]["ACCEPTNO"]."' ";
                    $db->query($query);

                    for ($fsCount = 1; $fsCount <= 4; $fsCount++) {
                        if (!strlen(trim($data_arr[$i]["FS_ORG_SHDIV".$fsCount]))) {
                            continue;
                        }
                        $data = array();
                        $data["ENTEXAMYEAR"][TEXT]    = $data_arr[$i]["ENTEXAMYEAR"];
                        $data["TESTDIV"][TEXT]        = $data_arr[$i]["TESTDIV"];
                        $data["ACCEPTNO"][TEXT]       = $data_arr[$i]["ACCEPTNO"];
                        $data["DATADIV"][TEXT]        = "1";
                        $data["WISHNO"][TEXT]         = $fsCount;
                        $data["ORG_SHDIV"][TEXT]      = $data_arr[$i]["FS_ORG_SHDIV".$fsCount];
                        $data["ORG_MAJORCD"][TEXT]    = $data_arr[$i]["FS_ORG_MAJORCD".$fsCount];
                        $data["ORG_JUDGEMENT"][TEXT]  = $data_arr[$i]["FS_ORG_JUDGEMENT".$fsCount];
                        $data["SHDIV"][TEXT]          = $data_arr[$i]["FS_SHDIV".$fsCount];
                        $data["COURSECD"][TEXT]       = $data_arr[$i]["FS_COURSECD".$fsCount];
                        $data["MAJORCD"][TEXT]        = $data_arr[$i]["FS_MAJORCD".$fsCount];
                        $data["EXAMCOURSECD"][TEXT]   = $data_arr[$i]["FS_EXAMCOURSECD".$fsCount];
                        $data["JUDGEMENT"][TEXT]      = $data_arr[$i]["FS_JUDGEMENT".$fsCount];
                        $data["REGISTERCD"][TEXT]     = STAFFCD;
                        $data["UPDATED"][FUNC]        = "SYSDATE()";

                        $query = Query::insertSQL($data, "ENTEXAM_CONSULTATION_DAT");
                        $db->query($query);
                    }

                    for ($psCount = 1; $psCount <= 4; $psCount++) {
                        if (!strlen(trim($data_arr[$i]["PS_ORG_SHDIV".$psCount]))) {
                            continue;
                        }
                        $data = array();
                        $data["ENTEXAMYEAR"][TEXT]    = $data_arr[$i]["ENTEXAMYEAR"];
                        $data["TESTDIV"][TEXT]        = $data_arr[$i]["TESTDIV"];
                        $data["ACCEPTNO"][TEXT]       = $data_arr[$i]["ACCEPTNO"];
                        $data["DATADIV"][TEXT]        = "2";
                        $data["WISHNO"][TEXT]         = $psCount;
                        $data["ORG_SHDIV"][TEXT]      = $data_arr[$i]["PS_ORG_SHDIV".$psCount];
                        $data["ORG_MAJORCD"][TEXT]    = $data_arr[$i]["PS_ORG_MAJORCD".$psCount];
                        $data["ORG_JUDGEMENT"][TEXT]  = $data_arr[$i]["PS_ORG_JUDGEMENT".$psCount];
                        $data["SHDIV"][TEXT]          = $data_arr[$i]["PS_SHDIV".$psCount];
                        $data["COURSECD"][TEXT]       = $data_arr[$i]["PS_COURSECD".$psCount];
                        $data["MAJORCD"][TEXT]        = $data_arr[$i]["PS_MAJORCD".$psCount];
                        $data["EXAMCOURSECD"][TEXT]   = $data_arr[$i]["PS_EXAMCOURSECD".$psCount];
                        $data["JUDGEMENT"][TEXT]      = $data_arr[$i]["PS_JUDGEMENT".$psCount];
                        $data["REGISTERCD"][TEXT]     = STAFFCD;
                        $data["UPDATED"][FUNC]        = "SYSDATE()";

                        $query = Query::insertSQL($data, "ENTEXAM_CONSULTATION_DAT");
                        $db->query($query);
                    }

                    $query  = " DELETE FROM ";
                    $query .= "       ENTEXAM_APPLICANTCONS_DAT ";
                    $query .= " WHERE ";
                    $query .= "     ENTEXAMYEAR = '".$data_arr[$i]["ENTEXAMYEAR"]."' ";
                    $query .= "     AND TESTDIV = '".$data_arr[$i]["TESTDIV"]."' ";
                    $query .= "     AND EXAMNO  = '".$data_arr[$i]["EXAMNO"]."' ";
                    $db->query($query);

                    for ($courseCount = 1; $courseCount <= 4; $courseCount++) {
                        if (!strlen(trim($data_arr[$i]["SHDIV".$courseCount]))) {
                            break;
                        }
                        $data = array();
                        $data["ENTEXAMYEAR"][TEXT]    = $data_arr[$i]["ENTEXAMYEAR"];
                        $data["TESTDIV"][TEXT]        = $data_arr[$i]["TESTDIV"];
                        $data["EXAMNO"][TEXT]         = $data_arr[$i]["EXAMNO"];
                        $data["SHDIV"][TEXT]          = $data_arr[$i]["SHDIV".$courseCount];
                        $data["COURSECD"][TEXT]       = $data_arr[$i]["COURSECD".$courseCount];
                        $data["MAJORCD"][TEXT]        = $data_arr[$i]["MAJORCD".$courseCount];
                        $data["EXAMCOURSECD"][TEXT]   = $data_arr[$i]["EXAMCOURSECD".$courseCount];
                        $data["JUDGEMENT"][TEXT]      = $data_arr[$i]["JUDGEMENT".$courseCount];
                        $data["REGISTERCD"][TEXT]     = STAFFCD;
                        $data["UPDATED"][FUNC]        = "SYSDATE()";

                        $query = Query::insertSQL($data, "ENTEXAM_APPLICANTCONS_DAT");
                        $db->query($query);
                    }
                    break;

                case "03"://志願者（基礎）データ
                    $query  = " DELETE FROM ";
                    $query .= "       ENTEXAM_APPLICANTCONFRPT_DAT ";
                    $query .= " WHERE ";
                    $query .= "     ENTEXAMYEAR  = '".$data_arr[$i]["ENTEXAMYEAR"]."' ";
                    $query .= "     AND TESTDIV  = '".$data_arr[$i]["TESTDIV"]."' ";
                    $query .= "     AND EXAMNO   = '".$data_arr[$i]["EXAMNO"]."' ";
                    $db->query($query);

                    $data = array();
                    $data["ENTEXAMYEAR"][TEXT]            = $data_arr[$i]["ENTEXAMYEAR"];
                    $data["TESTDIV"][TEXT]                = $data_arr[$i]["TESTDIV"];
                    $data["EXAMNO"][TEXT]                 = $data_arr[$i]["EXAMNO"];
                    $data["CONFIDENTIAL_RPT01"][NUMBER]   = $data_arr[$i]["CONFIDENTIAL_RPT01"];
                    $data["CONFIDENTIAL_RPT02"][NUMBER]   = $data_arr[$i]["CONFIDENTIAL_RPT02"];
                    $data["CONFIDENTIAL_RPT03"][NUMBER]   = $data_arr[$i]["CONFIDENTIAL_RPT03"];
                    $data["CONFIDENTIAL_RPT04"][NUMBER]   = $data_arr[$i]["CONFIDENTIAL_RPT04"];
                    $data["CONFIDENTIAL_RPT05"][NUMBER]   = $data_arr[$i]["CONFIDENTIAL_RPT05"];
                    $data["CONFIDENTIAL_RPT06"][NUMBER]   = $data_arr[$i]["CONFIDENTIAL_RPT06"];
                    $data["CONFIDENTIAL_RPT07"][NUMBER]   = $data_arr[$i]["CONFIDENTIAL_RPT07"];
                    $data["CONFIDENTIAL_RPT08"][NUMBER]   = $data_arr[$i]["CONFIDENTIAL_RPT08"];
                    $data["CONFIDENTIAL_RPT09"][NUMBER]   = $data_arr[$i]["CONFIDENTIAL_RPT09"];
                    $data["CONFIDENTIAL_RPT10"][NUMBER]   = $data_arr[$i]["CONFIDENTIAL_RPT10"];
                    $data["CONFIDENTIAL_RPT11"][NUMBER]   = $data_arr[$i]["CONFIDENTIAL_RPT11"];
                    $data["CONFIDENTIAL_RPT12"][NUMBER]   = $data_arr[$i]["CONFIDENTIAL_RPT12"];
                    $data["TOTAL_REPORT"][NUMBER]         = $data_arr[$i]["TOTAL_REPORT"];
                    $data["AVERAGE5"][NUMBER]             = $data_arr[$i]["AVERAGE5"];
                    $data["AVERAGE_ALL"][NUMBER]          = $data_arr[$i]["AVERAGE_ALL"];
                    $data["REGISTERCD"][TEXT]             = STAFFCD;
                    $data["UPDATED"][FUNC]                = "SYSDATE()";

                    $query = Query::insertSQL($data, "ENTEXAM_APPLICANTCONFRPT_DAT");
                    $db->query($query);
                    break;
            }

            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }
}
