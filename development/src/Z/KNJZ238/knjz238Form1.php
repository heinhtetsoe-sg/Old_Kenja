<?php

require_once('for_php7.php');

class knjz238Form1 {
    function main(&$model) {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjz238index.php", "", "edit");

        //権限チェック
        authCheck($arg);

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["year"] = CTRL_YEAR . "年度";

        //リスト作成
        makeList($arg, $db, $model);

        //コピーボタン
        $extra = "onclick=\"return btn_submit('copy');\"";
        $arg["button"]["copy"] = knjCreateBtn($objForm, "btn_copy", "前年度からデータをコピー", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjz238Form1.html", $arg); 
    }
}

//リスト作成
function makeList(&$arg, $db, $model) {
    $bifKey = "";
    //教育課程対応
    if ($model->Properties["useCurriculumcd"] == '1') {
        //画面上の表示用
        $arg["CURRICULUM_CD"] = "1";
    } else {
        //画面上の表示用
        $arg["NO_CURRICULUM_CD"] = "1";
    }
    $query = knjz238Query::getList($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        array_walk($row, "htmlspecialchars_array");
        if ($bifKey !== $row["SPECIAL_GROUP_CD"]) {
            $cnt = $db->getOne(knjz238Query::getSpecialCnt($model, $row["SPECIAL_GROUP_CD"]));
            $row["ROWSPAN"] = $cnt > 0 ? $cnt : 1;
        }
        $bifKey = $row["SPECIAL_GROUP_CD"];
        $arg["data"][] = $row;
    }
    $result->free();
}

//権限チェック
function authCheck(&$arg) {
    if (AUTHORITY != DEF_UPDATABLE) {
        $arg["jscript"] = "OnAuthError();";
    }
}

?>
