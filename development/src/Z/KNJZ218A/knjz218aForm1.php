<?php

require_once('for_php7.php');

class knjz218aForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("list", "POST", "knjz218aindex.php", "", "list");

        //権限チェック
        authCheck($arg);

        //DB接続
        $db = Query::dbCheckOut();

        //年度、学期表示
        $arg["YEAR"] = CTRL_YEAR."年度";

        //ボタン作成
        makeBtn($objForm, $arg);

        //コースグループリスト
        makeCourseGroupList($arg, $db, $model);

        //hidden
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjz218aForm1.html", $arg); 
    }
}

//権限チェック
function authCheck(&$arg) {
    if (AUTHORITY != DEF_UPDATABLE) {
        $arg["jscript"] = "OnAuthError();";
    }
}

//コースグループリスト
function makeCourseGroupList(&$arg, $db, $model) {
    $g_cnt = $c_cnt = 1;
    $result = $db->query(knjz218aQuery::getCourseGroupList($model));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        //行数取得
        $grade_cnt  = $db->getOne(knjz218aQuery::getGradeCnt($model, $row["GRADE"]));
        $course_cnt = $db->getOne(knjz218aQuery::getCourseGroupCnt($model, $row["GRADE"], $row["GROUP_CD"]));

        $row["GROUP_CD"] = View::alink("knjz218aindex.php",
                            $row["GROUP_CD"],
                            "target=right_frame",
                            array("cmd"         => "sel",
                                  "SEND_FLG"    => "1",
                                  "GRADE"       => $row["GRADE"],
                                  "GROUP_CD"    => $row["GROUP_CD"]));


        if ($g_cnt == 1) $row["ROWSPAN1"] = $grade_cnt;     //学年の行数
        if ($c_cnt == 1) $row["ROWSPAN2"] = $course_cnt;    //コースグループの行数

        $arg["data"][] = $row;

        if ($g_cnt == $grade_cnt) {
            $g_cnt = 1;
        } else {
            $g_cnt++;
        }
        if ($c_cnt == $course_cnt) {
            $c_cnt = 1;
        } else {
            $c_cnt++;
        }
    }
    $result->free();

}

//ボタン作成
function makeBtn(&$objForm, &$arg) {
    //コピーボタン
    $extra = "onclick=\"return btn_submit('copy');\"";
    $arg["COPYBTN"] = knjCreateBtn($objForm, "COPYBTN", "前年度からコピー", $extra);
}
?>
