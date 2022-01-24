<?php

require_once('for_php7.php');

class knjx090equery extends Query {
   //学校種別取得
    function getSchoolCd() {
        $query  = " SELECT ";
        $query .= "     NAME2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' AND ";
        $query .= "     NAMECD2 = '00' ";

        return $query;
    }

    //年度・学期一覧取得
    function getYearSemeList() {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.YEAR || T1.SEMESTER AS VALUE, ";
        $query .= "     T1.YEAR || '年度 ' || T2.SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     CHAIR_STD_DAT T1 ";
        $query .= "     INNER JOIN SEMESTER_MST T2 ON T1.YEAR = T2.YEAR AND T1.SEMESTER = T2.SEMESTER ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //旧年度・学期取得
    function getYearSemeListOld($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.YEAR || T1.SEMESTER ";
        $query .= " FROM ";
        $query .= "     CHAIR_STD_DAT T1 ";
        $query .= "     INNER JOIN SEMESTER_MST T2 ON T1.YEAR = T2.YEAR AND T1.SEMESTER = T2.SEMESTER ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR || T1.SEMESTER < '".$model->field["YEAR_SEMESTER"]."' ";
        $query .= " ORDER BY ";
        $query .= " T1.YEAR || T1.SEMESTER DESC ";
        $query .= " FETCH FIRST 1 ROWS ONLY ";

        return $query;
    }

    //存在チェック（年度・学期）
    function getYearSemesterMst($year, $semester) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '".$year."' AND ";
        $query .= "     SEMESTER = '".$semester."' ";

        return $query;
    }

    //存在チェック -- CHAIR_STD_DAT
    function checkChairStdDat($data_arr, $chaircd) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     CHAIR_STD_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$data_arr["YEAR"]."' AND ";
        $query .= "     SEMESTER    = '".$data_arr["SEMESTER"]."' AND ";
        $query .= "     CHAIRCD     = '".$chaircd."' AND ";
        $query .= "     APPDATE     = '".$data_arr["APPDATE"]."' AND ";
        $query .= "     SCHREGNO    = '".$data_arr["SCHREGNO"]."' ";

        return $query;
    }

    //更新（ＣＳＶ取込）
    function getUpdateQuery($db, $data_arr) {
        $cnt = 0;   //処理件数

        for ($i = 0; $i < get_count($data_arr); $i++) {
            for ($j = 1; $j <= $data_arr[$i]["COL_NO"]; $j++) {
                //データ有無チェック -- CHAIR_STD_DAT
                $check_cnt = $db->getOne(knjx090eQuery::checkChairStdDat($data_arr[$i], $data_arr[$i]["CHAIRCD".$j]));
                $flg = ($check_cnt == 0) ? "insert" : "update";

                $data = array();
                if ($flg == "insert") {
                    $data["YEAR"][TEXT]            = $data_arr[$i]["YEAR"];
                    $data["SEMESTER"][TEXT]        = $data_arr[$i]["SEMESTER"];
                    $data["CHAIRCD"][TEXT]         = $data_arr[$i]["CHAIRCD".$j];
                    $data["SCHREGNO"][TEXT]        = $data_arr[$i]["SCHREGNO"];
                    $data["APPDATE"][TEXT]         = $data_arr[$i]["APPDATE"];
                }
                $data["APPENDDATE"][TEXT]      = $data_arr[$i]["APPENDDATE"];
                $data["ROW"][TEXT]             = $data_arr[$i]["ROW".$j];
                $data["COLUMN"][TEXT]          = $data_arr[$i]["COLUMN".$j];
                $data["REGISTERCD"][TEXT]      = STAFFCD;
                $data["UPDATED"][NUMBER]       = "sysdate()";

                if ($flg == "insert") {
                    $query = Query::insertSQL($data, "CHAIR_STD_DAT");
                } else {
                    $where  = " WHERE ";
                    $where .= "     YEAR        = '".$data_arr[$i]["YEAR"]."' AND ";
                    $where .= "     SEMESTER    = '".$data_arr[$i]["SEMESTER"]."' AND ";
                    $where .= "     CHAIRCD     = '".$data_arr[$i]["CHAIRCD".$j]."' AND ";
                    $where .= "     APPDATE     = '".$data_arr[$i]["APPDATE"]."' AND ";
                    $where .= "     SCHREGNO    = '".$data_arr[$i]["SCHREGNO"]."' ";

                    $query = Query::updateSQL($data, "CHAIR_STD_DAT", $where);
                }
                $db->query($query);
            }
            $cnt++;
        }
        return $cnt;
    }

    //削除（ＣＳＶ取込）
    function getDeleteQuery($db, $data_arr) {
        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            for ($j = 1; $j <= $data_arr[$i]["COL_NO"]; $j++) {
                $query  = " DELETE FROM ";
                $query .= "     CHAIR_STD_DAT ";
                $query .= " WHERE ";
                $query .= "     YEAR        = '".$data_arr[$i]["YEAR"]."' AND ";
                $query .= "     SEMESTER    = '".$data_arr[$i]["SEMESTER"]."' AND ";
                $query .= "     CHAIRCD     = '".$data_arr[$i]["CHAIRCD".$j]."' AND ";
                $query .= "     APPDATE     = '".$data_arr[$i]["APPDATE"]."' AND ";
                $query .= "     SCHREGNO    = '".$data_arr[$i]["SCHREGNO"]."' ";

                $db->query($query);
            }
            $cnt++;
        }
        return $cnt;
    }

    //講座の存在チェック
    function getChairDat($year, $semester) {
        $query  = " SELECT ";
        $query .= "     CHAIRCD ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '".$year."' AND ";
        $query .= "     SEMESTER = '".$semester."' ";

        return $query;
    }

    //日付チェック（学期内）
    function checkSemesterMst($year, $semester, $date) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '".$year."' AND ";
        $query .= "     SEMESTER = '".$semester."' AND ";
        $query .= "     '".$date."' BETWEEN SDATE AND EDATE ";

        return $query;
    }

    //在籍チェック
    function checkSchregno($year, $semester, $schregno) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '".$year."' AND ";
        $query .= "     SEMESTER = '".$semester."' AND ";
        $query .= "     SCHREGNO = '".$schregno."' ";

        return $query;
    }

    //エラーＤＢへの追加
    function insertQueryErr(&$db, $record_no, $check_error) {
        $data1 = array();
        $data1["PROGRAMID"][TEXT]   = "KNJX090E";
        $data1["MSGROW"][NUMBER]    = $record_no;
        $data1["MSGREMARK"][TEXT]   = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");
        $result = $db->query($query);
    }

    //学期マスタ取得（ヘッダ例出力用）
    function getSemesterMst() {
        $query  = " SELECT ";
        $query .= "     SDATE, ";
        $query .= "     EDATE ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER = '".CTRL_SEMESTER."' ";

        return $query;
    }

    //ＣＳＶデータ出力
    function getCsvQuery($model) {
        $query  = " WITH MAX_APPDATE AS ( ";
        $query .= "     SELECT ";
        $query .= "         CHAIRCD, ";
        $query .= "         SCHREGNO, ";
        $query .= "         MAX(APPDATE) AS APPDATE ";
        $query .= "     FROM ";
        $query .= "         CHAIR_STD_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR || SEMESTER = '".$model->field["YEAR_SEMESTER"]."' ";
        $query .= "     GROUP BY ";
        $query .= "         CHAIRCD, ";
        $query .= "         SCHREGNO ";
        $query .= " ), MAIN AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     SUBSTR(L1.HR_CLASS, 2, 1) || SUBSTR(L1.ATTENDNO, 1,2) AS OLD_HR_CLASS, ";
        $query .= "     L2.NAME AS NAME_SEI, ";
        $query .= "     L2.NAME, ";
        $query .= "     L2.SEX, ";
        $query .= "     SUBSTR(L1.HR_CLASS, 2, 1) AS HR_CLASS, ";
        $query .= "     L2.NAME_KANA AS NAME_KANA_SEI, ";
        $query .= "     L2.NAME_KANA AS NAME_KANA, ";
        $query .= "     L2.NAME_ENG, ";
        $query .= "     L1.COURSECODE, ";
        $query .= "     L5.COURSECODENAME, ";
        $query .= "     T1.CHAIRCD, ";
        $query .= "     L3.CHAIRNAME ";
        $query .= " FROM ";
        $query .= "     CHAIR_STD_DAT T1 ";
        $query .= "     LEFT JOIN SCHREG_REGD_DAT L1 ";
        $query .= "              ON L1.YEAR     = T1.YEAR ";
        $query .= "             AND L1.SEMESTER = T1.SEMESTER ";
        $query .= "             AND L1.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST L2 ";
        $query .= "              ON L2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN CHAIR_DAT L3 ";
        $query .= "              ON L3.YEAR     = T1.YEAR ";
        $query .= "             AND L3.SEMESTER = T1.SEMESTER ";
        $query .= "             AND L3.CHAIRCD  = T1.CHAIRCD ";
        $query .= "     LEFT JOIN SEMESTER_MST L4 ";
        $query .= "              ON L4.YEAR     = T1.YEAR ";
        $query .= "             AND L4.SEMESTER = T1.SEMESTER ";
        $query .= "     LEFT JOIN V_COURSECODE_MST L5 ";
        $query .= "              ON L5.YEAR       = T1.YEAR ";
        $query .= "             AND L5.COURSECODE = L1.COURSECODE ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR || T1.SEMESTER = '".$model->field["YEAR_SEMESTER"]."' AND ";
        $query .= "     EXISTS (SELECT ";
        $query .= "                 'X' ";
        $query .= "             FROM ";
        $query .= "                 MAX_APPDATE S1 ";
        $query .= "             WHERE ";
        $query .= "                 T1.CHAIRCD  = S1.CHAIRCD AND ";
        $query .= "                 T1.SCHREGNO = S1.SCHREGNO AND ";
        $query .= "                 T1.APPDATE  = S1.APPDATE ";
        $query .= "             ) ";
        $query .= "     AND L3.CHAIRCD IN (SELECT W1.CHAIRCD ";
        $query .= "         FROM CHAIR_CLS_DAT W1 ";
        $query .= "         WHERE W1.YEAR || W1.SEMESTER = '".$model->field["YEAR_SEMESTER"]."' ";
        $query .= "               AND W1.GROUPCD  = '0000' ";
        $query .= "         GROUP BY ";
        $query .= "                  W1.CHAIRCD ";
        $query .= "         HAVING COUNT(*) = 1 ) ";
        $query .= " ORDER BY ";
        $query .= "     SCHREGNO, ";
        $query .= "     CHAIRCD ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     SUBSTR(L1.HR_CLASS, 2, 1) || SUBSTR(L1.ATTENDNO, 1,2) AS OLD_HR_CLASS, ";
        $query .= "     T1.NAME_SEI, ";
        $query .= "     T1.NAME, ";
        $query .= "     T1.SEX, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.NAME_KANA_SEI, ";
        $query .= "     T1.NAME_KANA, ";
        $query .= "     T1.NAME_ENG, ";
        $query .= "     T1.COURSECODE, ";
        $query .= "     T1.COURSECODENAME, ";
        $query .= "     T1.CHAIRCD, ";
        $query .= "     T1.CHAIRNAME ";
        $query .= " FROM ";
        $query .= "     MAIN T1 ";
        $query .= "     LEFT JOIN SCHREG_REGD_DAT L1 ";
        $query .= "              ON L1.YEAR || L1.SEMESTER = '".$model->year_semester."' ";
        $query .= "             AND L1.SCHREGNO = T1.SCHREGNO ";

        return $query;
    }
}
?>
