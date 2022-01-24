<?php

require_once('for_php7.php');

class knjf140bquery extends Query {

    function getSecurityHigh() {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     MENU_HIGH_SECURITY_MST ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = 'KNJF140B' ";
        $query .= "     AND INVALID_FLG = '0' ";

        return $query;
    }

    function getSchoolCd() {
        $query  = " SELECT ";
        $query .= "     NAME2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' ";
        $query .= "     AND NAMECD2 = '00' ";

        return $query;
    }

    //年度一覧
    function getSelectFieldSQL() {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.YEAR || '年度 ' || T2.SEMESTERNAME AS LABEL, ";
        $query .= "     T1.YEAR || T1.SEMESTER AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        $query .= " INNER JOIN ";
        $query .= "     SEMESTER_MST T2 ON  T1.YEAR     = T2.YEAR ";
        $query .= "                     AND T1.SEMESTER = T2.SEMESTER ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組一覧
    function getSelectFieldSQL2($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     HR_NAME AS LABEL, ";
        $query .= "     GRADE || HR_CLASS AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR || SEMESTER = '{$model->field["YEAR"]}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //データ出力
    function getselectDataSQL($model) {
        $query  = " SELECT ";
        $query .= "     REGD.SCHREGNO, ";
        $query .= "     '' AS CODE1, ";
        $query .= "     REGD.GRADE AS CODE2, ";
        $query .= "     REGD.HR_CLASS AS CODE3, ";
        $query .= "     REGD.ATTENDNO AS CODE4, ";
        $query .= "     '' AS CODE5, ";
        $query .= "     '' AS CODE6, ";
        $query .= "     '' AS CODE7, ";
        $query .= "     '' AS CODE8, ";
        $query .= "     '' AS CODE9, ";
        $query .= "     '' AS CODE10, ";
        $query .= "     '' AS CODE11, ";
        $query .= "     MEDE_DD.HEIGHT, ";
        $query .= "     MEDE_DD.WEIGHT, ";
        $query .= "     '' AS CODE12, ";
        $query .= "     '' AS CODE13, ";
        $query .= "     '' AS CODE14, ";
        $query .= "     '' AS CODE15, ";
        $query .= "     '' AS CODE16, ";
        $query .= "     '' AS CODE17, ";
        $query .= "     '' AS CODE18, ";
        $query .= "     '' AS CODE19, ";
        $query .= "     F017_R.NAME3 AS R_BAREVISION_MARK, ";
        $query .= "     F017_L.NAME3 AS L_BAREVISION_MARK, ";
        $query .= "     '' AS CODE20, ";
        $query .= "     F017_VR.NAME3 AS R_VISION_MARK, ";
        $query .= "     F017_VL.NAME3 AS L_VISION_MARK, ";
        $query .= "     '' AS CODE21, ";
        $query .= "     '' AS CODE22, ";
        $query .= "     '' AS CODE23, ";
        $query .= "     '' AS CODE24, ";
        $query .= "     '' AS CODE25, ";
        $query .= "     '' AS CODE26, ";
        $query .= "     '' AS CODE27, ";
        $query .= "     '' AS CODE28, ";
        $query .= "     '' AS CODE29, ";
        $query .= "     '' AS CODE30, ";
        $query .= "     '' AS CODE31, ";
        $query .= "     F010_R.NAME3 AS R_EAR, ";
        $query .= "     F010_L.NAME3 AS L_EAR, ";
        $query .= "     '' AS CODE32, ";
        $query .= "     '' AS CODE33, ";
        $query .= "     '' AS CODE34, ";
        $query .= "     '' AS CODE35, ";
        $query .= "     '' AS CODE36, ";
        $query .= "     '' AS CODE37, ";
        $query .= "     '' AS CODE38, ";
        $query .= "     '' AS CODE39, ";
        $query .= "     '' AS CODE40, ";
        $query .= "     '' AS CODE41, ";
        $query .= "     '' AS CODE42, ";
        $query .= "     '' AS CODE43, ";
        $query .= "     '' AS CODE44, ";
        $query .= "     '' AS CODE45, ";
        $query .= "     '' AS CODE46, ";
        $query .= "     '' AS CODE47, ";
        $query .= "     '' AS CODE48, ";
        $query .= "     '' AS CODE49, ";
        $query .= "     '' AS CODE50, ";
        $query .= "     '' AS CODE51, ";
        $query .= "     '' AS CODE52, ";
        $query .= "     '' AS CODE53, ";
        $query .= "     '' AS CODE54, ";
        $query .= "     '' AS CODE55, ";
        $query .= "     '' AS CODE56, ";
        $query .= "     '' AS CODE57, ";
        $query .= "     '' AS CODE58, ";
        $query .= "     '' AS CODE59, ";
        $query .= "     '' AS CODE60, ";
        $query .= "     '' AS CODE61, ";
        $query .= "     '' AS CODE62, ";
        $query .= "     '' AS CODE63, ";
        $query .= "     '' AS CODE64, ";
        $query .= "     '' AS CODE65, ";
        $query .= "     '' AS CODE66, ";
        $query .= "     '' AS CODE67, ";
        $query .= "     '' AS CODE68, ";
        $query .= "     '' AS CODE69, ";
        $query .= "     '' AS CODE70, ";
        $query .= "     '' AS CODE71, ";
        $query .= "     '' AS CODE72, ";
        $query .= "     '' AS CODE73, ";
        $query .= "     '' AS CODE74, ";
        $query .= "     '' AS CODE75, ";
        $query .= "     '' AS CODE76, ";
        $query .= "     '' AS CODE77, ";
        $query .= "     '' AS CODE78, ";
        $query .= "     '' AS CODE79, ";
        $query .= "     '' AS CODE80, ";
        $query .= "     '' AS CODE81, ";
        $query .= "     '' AS CODE82, ";
        $query .= "     '' AS CODE83, ";
        $query .= "     '' AS CODE84, ";
        $query .= "     '' AS CODE85, ";
        $query .= "     '' AS CODE86, ";
        $query .= "     '' AS CODE87, ";
        $query .= "     '' AS CODE88, ";
        $query .= "     '' AS CODE89, ";
        $query .= "     '' AS CODE90, ";
        $query .= "     '' AS CODE91, ";
        $query .= "     '' AS CODE92, ";
        $query .= "     '' AS CODE93, ";
        $query .= "     '' AS CODE94, ";
        $query .= "     '' AS CODE95, ";
        $query .= "     '' AS CODE96, ";
        $query .= "     '' AS CODE97, ";
        $query .= "     '' AS CODE98, ";
        $query .= "     '' AS CODE99, ";
        $query .= "     '' AS CODE100, ";
        $query .= "     '' AS CODE101, ";
        $query .= "     '' AS CODE102, ";
        $query .= "     '' AS CODE103, ";
        $query .= "     '' AS CODE104, ";
        $query .= "     '' AS CODE105, ";
        $query .= "     '' AS CODE106, ";
        $query .= "     '' AS CODE107, ";
        $query .= "     '' AS CODE108, ";
        $query .= "     '' AS CODE109, ";
        $query .= "     '' AS CODE110, ";
        $query .= "     '' AS CODE111, ";
        $query .= "     '' AS CODE112, ";
        $query .= "     '' AS CODE113, ";
        $query .= "     '' AS CODE114, ";
        $query .= "     '' AS CODE115, ";
        $query .= "     '' AS CODE116, ";
        $query .= "     '' AS CODE117, ";
        $query .= "     '' AS CODE118, ";
        $query .= "     '' AS CODE119, ";
        $query .= "     '' AS CODE120, ";
        $query .= "     '' AS CODE121, ";
        $query .= "     '' AS CODE122, ";
        $query .= "     '' AS CODE123, ";
        $query .= "     '' AS CODE124, ";
        $query .= "     '' AS CODE125, ";
        $query .= "     '' AS CODE126, ";
        $query .= "     '' AS CODE127, ";
        $query .= "     '' AS CODE128, ";
        $query .= "     '' AS CODE129, ";
        $query .= "     '' AS CODE130, ";
        $query .= "     '' AS CODE131, ";
        $query .= "     '' AS CODE132, ";
        $query .= "     '' AS CODE133, ";
        $query .= "     '' AS CODE134, ";
        $query .= "     '' AS CODE135 ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT REGD, ";
        $query .= "     MEDEXAM_DET_DAT MEDE_DD ";
        $query .= "     LEFT JOIN NAME_MST F010_R ON F010_R.NAMECD1 = 'F010' ";
        $query .= "          AND MEDE_DD.R_EAR = F010_R.NAMECD2 ";
        $query .= "     LEFT JOIN NAME_MST F010_L ON F010_L.NAMECD1 = 'F010' ";
        $query .= "          AND MEDE_DD.L_EAR = F010_L.NAMECD2 ";
        $query .= "     LEFT JOIN NAME_MST F017_R ON F017_R.NAMECD1 = 'F017' ";
        $query .= "          AND MEDE_DD.R_BAREVISION_MARK = F017_R.NAMECD2 ";
        $query .= "     LEFT JOIN NAME_MST F017_L ON F017_L.NAMECD1 = 'F017' ";
        $query .= "          AND MEDE_DD.L_BAREVISION_MARK = F017_L.NAMECD2 ";
        $query .= "     LEFT JOIN NAME_MST F017_VR ON F017_VR.NAMECD1 = 'F017' ";
        $query .= "          AND MEDE_DD.R_VISION_MARK = F017_VR.NAMECD2 ";
        $query .= "     LEFT JOIN NAME_MST F017_VL ON F017_VL.NAMECD1 = 'F017' ";
        $query .= "          AND MEDE_DD.L_VISION_MARK = F017_VL.NAMECD2 ";

        $query .= " WHERE REGD.YEAR || REGD.SEMESTER = '{$model->field["YEAR"]}' ";
        if ($model->field["GRADE_HR_CLASS"] != "") {
            $query .= "AND REGD.GRADE || REGD.HR_CLASS = '{$model->field["GRADE_HR_CLASS"]}' ";
        }
        $query .= "    AND REGD.YEAR     = MEDE_DD.YEAR ";
        $query .= "    AND REGD.SCHREGNO = MEDE_DD.SCHREGNO ";
        $query .= " ORDER BY ";
        $query .= "     REGD.GRADE, ";
        $query .= "     REGD.HR_CLASS, ";
        $query .= "     REGD.ATTENDNO, ";
        $query .= "     REGD.SCHREGNO ";

        return $query;
    }


    //視力も文字取得
    function getExistNameMst($namecd1, $name3) {
        $query  = " SELECT ";
        $query .= "     NAMECD2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = '{$namecd1}' AND ";
        $query .= "     NAME3 = '{$name3}' ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";
        $query .= " FETCH FIRST 1 ROWS ONLY ";

        return $query;
    }

    //在籍チェック
    function getRegdCheck($data) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT ";
        $query .= " WHERE ";
        $query .= "      SCHREGNO = '{$data["SCHREGNO"]}' ";
        $query .= "  AND YEAR   = '{$data["YEAR"]}' ";

        return $query;
    }

    //マスタデータの削除
    function deleteQueryCsv(&$db, $data) {
        //一般の削除
        $query  = " DELETE FROM ";
        $query .= "     MEDEXAM_DET_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '{$data["YEAR"]}' ";
        $query .= " AND SCHREGNO = '{$data["SCHREGNO"]}' ";
        $db->query($query);

        //MEDEXAM_HDATの削除
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     MEDEXAM_TOOTH_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$data["YEAR"]}' ";
        $query .= " AND SCHREGNO = '{$data["SCHREGNO"]}' ";

        if ($db->getOne($query) == 0) {
            $query  = " DELETE FROM ";
            $query .= "     MEDEXAM_HDAT ";
            $query .= " WHERE ";
            $query .= "     YEAR     = '{$data["YEAR"]}' ";
            $query .= " AND SCHREGNO = '{$data["SCHREGNO"]}' ";
            $db->query($query);
        } else {
            $data2["YEAR"][TEXT]       = $data["YEAR"];
            $data2["SCHREGNO"][TEXT]   = $data["SCHREGNO"];
            $data2["DATE"][TEXT]       = "";
            $data2["REGISTERCD"][TEXT] = STAFFCD;
            $data2["UPDATED"][NUMBER]  = "sysdate()";

            $where  = " WHERE ";
            $where .= "     YEAR     = '{$data["YEAR"]}' ";
            $where .= " AND SCHREGNO = '{$data["SCHREGNO"]}' ";

            $query = Query::updateSQL($data2, "MEDEXAM_HDAT",$where);
            $db->query($query);
        }
    }

    //マスタの追加（ＣＳＶデータより読込）
    function deleteinsertQueryCsv(&$db, $data) {

        //一般の削除
        $query  = " DELETE FROM ";
        $query .= "     MEDEXAM_DET_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '{$data["YEAR"]}' ";
        $query .= " AND SCHREGNO = '{$data["SCHREGNO"]}' ";
        $db->query($query);

        $data1["YEAR"][TEXT]                 = $data["YEAR"];
        $data1["SCHREGNO"][TEXT]             = $data["SCHREGNO"];
        $data1["HEIGHT"][NUMBER]             = $data["HEIGHT"];
        $data1["WEIGHT"][NUMBER]             = $data["WEIGHT"];
        $data1["R_BAREVISION_MARK"][TEXT]    = $data["R_BAREVISION_MARK"];
        $data1["L_BAREVISION_MARK"][TEXT]    = $data["L_BAREVISION_MARK"];
        $data1["R_VISION_MARK"][TEXT]        = $data["R_VISION_MARK"];
        $data1["L_VISION_MARK"][TEXT]        = $data["L_VISION_MARK"];
        $data1["R_EAR"][TEXT]                = $data["R_EAR"];
        $data1["L_EAR"][TEXT]                = $data["L_EAR"];
        $data1["REGISTERCD"][TEXT]           = STAFFCD;
        $data1["UPDATED"][NUMBER]            = "sysdate()";

        $query = Query::insertSQL($data1, "MEDEXAM_DET_DAT");

        $db->query($query);

        //MEDEXAM_HDATの更新(あればアップデートなければインサート)
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     MEDEXAM_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '{$data["YEAR"]}' ";
        $query .= " AND SCHREGNO = '{$data["SCHREGNO"]}' ";
        $countHdat = $db->getOne($query);

        $data2["YEAR"][TEXT]       = $data["YEAR"];
        $data2["SCHREGNO"][TEXT]   = $data["SCHREGNO"];
        $data2["DATE"][TEXT]       = $data["DATE"];
        $data2["REGISTERCD"][TEXT] = STAFFCD;
        $data2["UPDATED"][NUMBER]  = "sysdate()";

        if ($countHdat > 0) { //レコードがあればアップデート
            $where  = " WHERE ";
            $where .= "     YEAR     = '{$data["YEAR"]}' ";
            $where .= " AND SCHREGNO = '{$data["SCHREGNO"]}' ";

            $query = Query::updateSQL($data2, "MEDEXAM_HDAT", $where);
        } else {
            $query = Query::insertSQL($data2, "MEDEXAM_HDAT");
        }

        $db->query($query);
    }

    //エラーＤＢへの追加
    function insertQueryErr(&$db, $record_no, $check_error)
    {
        $data1 = array();
        $data1["PROGRAMID"][TEXT] = PROGRAMID;
        $data1["MSGROW"][NUMBER]  = $record_no;
        $data1["MSGREMARK"][TEXT] = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");

        $result = $db->query($query);
    }
}
?>
