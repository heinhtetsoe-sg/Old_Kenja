<?php

require_once('for_php7.php');

class knjz204Form1 {
    function main(&$model) {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjz204index.php", "", "edit");

        //権限チェック
        authCheck($arg);

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["YEAR"] = CTRL_YEAR;

        //授業時数管理区分
        $query = knjz204Query::getVNameMstZ042();
        $jugyouJisuFlg = $db->getOne($query);
        $arg["JUGYOU_JISU_FLG"] = $jugyouJisuFlg;

        //コピーボタン
        $extra = "onclick=\"return btn_submit('copy');\"";
        $arg["button"]["btn_copy"] = knjCreateBtn($objForm, "btn_copy", "前年度からコピー", $extra);

        //リスト作成
        makeList($arg, $db, $model);

        //hidden
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjz204Form1.html", $arg); 
    }
}

//リスト作成
function makeList(&$arg, $db, $model) {
    $bifKey = "";
    $result = $db->query(knjz204Query::getList());
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        array_walk($row, "htmlspecialchars_array");
        if ($bifKey !== $row["PATTERNCD"]) {
            $cnt = $db->getOne(knjz204Query::getAssesslevelCnt($row["PATTERNCD"]));
            $row["ROWSPAN"] = $cnt > 0 ? $cnt : 1;
        }
        $row["RATE"] = floor($row["RATE"]);
        $row["BASEDATE"] = str_replace("-","/",$row["BASEDATE"]);
        $bifKey  = $row["PATTERNCD"];
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
