<?php

require_once('for_php7.php');

class knjj120Query extends Query
{
    //校種取得
    function getSchkind($model) {
        $query  = " SELECT ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     NAME1 ";

        return $query;
    }

    //部クラブ取得
    function getClubList($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.CLUBCD AS VALUE, ";
        $query .= "     T1.CLUBCD || ' ' || T1.CLUBNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     CLUB_MST T1, ";
        $query .= "     CLUB_YDAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.CLUBCD = T2.CLUBCD AND ";
        $query .= "     T2.YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND T1.SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= " AND T1.SCHOOL_KIND = '".$model->field["SCHKIND"]."' ";
            $query .= " AND T2.SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= " AND T2.SCHOOL_KIND = '".$model->field["SCHKIND"]."' ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T1.SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= " AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
            $query .= " AND T2.SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        //更新可能(制限付)
        if(AUTHORITY != DEF_UPDATABLE){
            $query .= "    AND T1.CLUBCD IN (SELECT ";
            $query .= "                          CLUBCD ";
            $query .= "                      FROM ";
            $query .= "                          CLUB_ADVISER_DAT ";
            $query .= "                      WHERE ";
            $query .= "                          YEAR = '".CTRL_YEAR."' ";
            $query .= "                          AND ADVISER = '".STAFFCD."' ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                $query .= " AND SCHOOLCD    = '".SCHOOLCD."' ";
                $query .= " AND SCHOOL_KIND = '".$model->field["SCHKIND"]."' ";
            } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= " AND SCHOOLCD    = '".SCHOOLCD."' ";
                $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
            $query .= "                     ) ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //部員一覧取得
    function getClubMember($model, $div)
    {
        $sdate = CTRL_YEAR.'-04-01';
        $edate = (CTRL_YEAR+1).'-03-31';

        $query  = " WITH SCH_INFO AS( ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T3.HR_NAME, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T2.NAME_SHOW ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1  ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T1.YEAR AND GDAT.GRADE = T1.GRADE ";
            $query .= " AND GDAT.SCHOOL_KIND = '".$model->field["SCHKIND"]."' ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T1.YEAR AND GDAT.GRADE = T1.GRADE ";
            $query .= " AND GDAT.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "     LEFT JOIN SCHREG_BASE_MST T2 ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT T3 ON ";
        $query .= "         T1.YEAR     = T3.YEAR AND ";
        $query .= "         T1.SEMESTER = T3.SEMESTER AND ";
        $query .= "         T1.GRADE    = T3.GRADE AND ";
        $query .= "         T1.HR_CLASS = T3.HR_CLASS ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= " ) ";

        $query .= " SELECT DISTINCT ";
        $query .= "     T2.GRADE || T2.HR_CLASS || T2.ATTENDNO || T2.SCHREGNO AS VALUE, ";
        $query .= "     T2.HR_NAME || ' ' || T2.ATTENDNO || '番 ' || T2.NAME_SHOW AS LABEL, ";
        $query .= "     T2.GRADE, ";
        $query .= "     T2.HR_CLASS, ";
        $query .= "     T2.ATTENDNO ";
        $query .= " FROM ";
        $query .= "     SCHREG_CLUB_HIST_DAT T1, ";
        $query .= "     SCH_INFO T2 ";
        $query .= " WHERE ";
        $query .= "     T1.CLUBCD = '".$model->field["CLUBCD"]."' AND ";
        $query .= "     T1.SCHREGNO = T2.SCHREGNO AND ";
        $query .= "     '".$sdate."' < CASE WHEN T1.EDATE IS NULL THEN '9999-12-31' ELSE T1.EDATE END AND ";
        $query .= "     '".$edate."' > T1.SDATE ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND T1.SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= " AND T1.SCHOOL_KIND = '".$model->field["SCHKIND"]."' ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T1.SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= " AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        if($model->field["SCHREGNO"]){
            if($div == "right") {
                $query .= "     AND T2.GRADE || T2.HR_CLASS || T2.ATTENDNO || T2.SCHREGNO ";
                $query .= "     NOT IN ('".implode(explode(',',$model->field["SCHREGNO"]),"','")."') ";
            } else {
                $query .= "     AND T2.GRADE || T2.HR_CLASS || T2.ATTENDNO || T2.SCHREGNO ";
                $query .= "     IN ('".implode(explode(',',$model->field["SCHREGNO"]),"','")."') ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     T2.GRADE, ";
        $query .= "     T2.HR_CLASS, ";
        $query .= "     T2.ATTENDNO ";

        return $query;
    }

    //開催地域取得
    function getClubHost($model)
    {
        $query  = " SELECT ";
        $query .= "     HOSTCD || ' ' || HOSTNAME AS LABEL, ";
        $query .= "     HOSTCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     CLUB_HOST_MST ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " WHERE SCHOOLCD  = '".SCHOOLCD."' ";
            $query .= " AND SCHOOL_KIND = '".$model->field["SCHKIND"]."' ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " WHERE SCHOOLCD  = '".SCHOOLCD."' ";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //種目取得
    function getClubItem($model, $clubcd)
    {
        $query  = " SELECT ";
        $query .= "     T1.ITEMCD || ' ' || T2.ITEMNAME AS LABEL, ";
        $query .= "     T1.ITEMCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     CLUB_ITEM_DAT T1 ";
        $query .= "     LEFT JOIN CLUB_ITEM_MST T2 ON T1.ITEMCD = T2.ITEMCD ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND T2.SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= " AND T2.SCHOOL_KIND = '".$model->field["SCHKIND"]."' ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T2.SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " WHERE ";
        $query .= "     T1.CLUBCD = '".$clubcd."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND T1.SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= " AND T1.SCHOOL_KIND = '".$model->field["SCHKIND"]."' ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T1.SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= " AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //種類取得
    function getClubItemKind($model, $itemcd)
    {
        $query  = " SELECT ";
        $query .= "     KINDCD || ' ' || KINDNAME AS LABEL, ";
        $query .= "     KINDCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     CLUB_ITEM_KIND_MST ";
        $query .= " WHERE ";
        $query .= "     ITEMCD = '".$itemcd."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= " AND SCHOOL_KIND = '".$model->field["SCHKIND"]."' ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //成績取得
    function getClubRecord($model)
    {
        $query  = " SELECT ";
        $query .= "     RECORDCD || ' ' || RECORDNAME AS LABEL, ";
        $query .= "     RECORDCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     CLUB_RECORD_MST ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " where SCHOOLCD  = '".SCHOOLCD."' ";
            $query .= " AND SCHOOL_KIND = '".$model->field["SCHKIND"]."' ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " where SCHOOLCD  = '".SCHOOLCD."' ";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //更新
    function &UpdateQuery($model, &$upfilename)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $model->field["DETAIL_DATE"] = str_replace("/", "-", $model->field["DETAIL_DATE"]);

        $cnt = 0;

        $schregno = explode(',', $model->field["SCHREGNO"]);
        if($model->field["SCHREGNO"]){
            for ($i = 0; $i < get_count($schregno); $i++){
                $fnamewk = "";
                $schregno[$i] = substr($schregno[$i],8);

                //部クラブ在籍チェック
                $check = $db->getOne(knjj120Query::checkHistData($model, $schregno[$i]));

                if($check == 0) continue;

                //DETAIL_SEQ取得
                $query = knjj120Query::getDetailSeq($model, $schregno[$i]);
                $detail_seq = ($db->getOne($query)) ? $db->getOne($query)+1 : 1 ;

                //追加
                if ($model->Properties["use_prg_schoolkind"] == "1") {
                    $data["SCHOOLCD"][TEXT]        = SCHOOLCD;
                    $data["SCHOOL_KIND"][TEXT]     = $model->field["SCHKIND"];
                    $fnamewk = SCHOOLCD . $model->field["SCHKIND"];
                } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                    $data["SCHOOLCD"][TEXT]        = SCHOOLCD;
                    $data["SCHOOL_KIND"][TEXT]     = SCHOOLKIND;
                    $fnamewk = SCHOOLCD . SCHOOLKIND;
                }
                $data["SCHREGNO"][TEXT]         = $schregno[$i];
                $data["CLUBCD"][TEXT]           = $model->field["CLUBCD"];
                $data["DETAIL_DATE"][TEXT]      = $model->field["DETAIL_DATE"];
                $data["DETAIL_SEQ"][NUMBER]     = $detail_seq;
                $data["MEET_NAME"][TEXT]        = $model->field["MEET_NAME"];
                $data["DIV"][TEXT]              = $model->field["DIV"];
                $data["HOSTCD"][TEXT]           = $model->field["HOSTCD"];
                $data["ITEMCD"][TEXT]           = $model->field["ITEMCD"];
                $data["KINDCD"][TEXT]           = $model->field["KINDCD"];
                $data["RECORDCD"][TEXT]         = $model->field["RECORDCD"];
                $data["DOCUMENT"][TEXT]         = $model->field["DOCUMENT"];
                $data["DETAIL_REMARK"][TEXT]    = $model->field["DETAIL_REMARK"];
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][FUNC]          = "sysdate()";

                $db->query(Query::insertSQL($data, "SCHREG_CLUB_HDETAIL_DAT"));
                
                //アップロード用のファイル名称を設定
                $upfilename[] = $fnamewk . $schregno[$i] . $model->field["CLUBCD"] . $model->field["DETAIL_DATE"] . $detail_seq;

                $cnt++;
            }
        }

        $db->commit();      // トランザクションをコミットする。
        Query::dbCheckIn($db);
        return $cnt;
    }

    //DETAIL_SEQ取得
    function getDetailSeq($model, $schregno)
    {
        $query  = " SELECT ";
        $query .= "     MAX(DETAIL_SEQ) AS DETAIL_SEQ ";
        $query .= " FROM ";
        $query .= "     SCHREG_CLUB_HDETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' AND ";
        $query .= "     CLUBCD = '".$model->field["CLUBCD"]."' AND ";
        $query .= "     DETAIL_DATE = '".$model->field["DETAIL_DATE"]."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= " AND SCHOOL_KIND = '".$model->field["SCHKIND"]."' ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        return $query;
    }
    //部クラブ在籍チェック
    function checkHistData($model, $schregno)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     SCHREG_CLUB_HIST_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' AND ";
        $query .= "     CLUBCD = '".$model->field["CLUBCD"]."' AND ";
        $query .= "     '".$model->field["DETAIL_DATE"]."' BETWEEN SDATE ";
        $query .= "         AND CASE WHEN EDATE IS NULL THEN '9999-12-31' ELSE EDATE END ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= " AND SCHOOL_KIND = '".$model->field["SCHKIND"]."' ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }

        return $query;
    }
}
?>
