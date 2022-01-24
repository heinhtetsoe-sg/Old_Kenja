<?php
class knjj512Form1
{
    public function main(&$model)
    {
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjj512index.php", "", "edit");

        //権限チェック
        authCheck($arg);

        //DB接続
        $db = Query::dbCheckOut();

        //リスト作成
        makeList($arg, $db, $model);

        //hidden
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjj512Form1.html", $arg);
    }
}

//リスト作成
function makeList(&$arg, $db, $model)
{
    $data = array();
    $i = $model->maxLebel;
    $query = knjj512Query::getList($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        array_walk($row, "htmlspecialchars_array");
        //年齢
        $data["AGE"] = $row["AGE"];

        //A～E
        $name = "TOTAL_SCORE_".$model->totalMark[$i];
        if ($model->totalMark[$i] == "A") {
            $data[$name] = $row["TOTAL_SCORE_HIGH"]."以上";
        } elseif ($model->totalMark[$i] == "E") {
            $data[$name] = $row["TOTAL_SCORE_LOW"]."以下";
        } else {
            $data[$name] = $row["TOTAL_SCORE_LOW"]."～".$row["TOTAL_SCORE_HIGH"];
        }
        if ($i == 1) {
            $arg["data"][] = $data;
            $data = array();
            $i = $model->maxLebel;
        } else {
            $i = $i-1;
        }
    }
    $result->free();
}

//権限チェック
function authCheck(&$arg)
{
    if (AUTHORITY != DEF_UPDATABLE) {
        $arg["jscript"] = "OnAuthError();";
    }
}
