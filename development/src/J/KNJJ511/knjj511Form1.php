<?php
class knjj511Form1
{
    public function main(&$model)
    {
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjj511index.php", "", "edit");

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
        View::toHTML($model, "knjj511Form1.html", $arg);
    }
}

//リスト作成
function makeList(&$arg, $db, $model)
{
    $data = array();
    $befItemCd = "";
    $query = knjj511Query::getList($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        array_walk($row, "htmlspecialchars_array");

        //種別の切り替わり
        if ($befItemCd != "" && $befItemCd != $row["ITEMCD"]) {
            $arg["data"][] = $data;
            $data = array();
        }

        //種別
        $data["ITEMCD"] = $row["ITEMCD"];
        //種別名称
        $data["ITEMNAME"] = $row["ITEMNAME"];

        //性別
        if ($row["SEX"] == "1") {
            $data["MAN_NAME"] = $row["NAME1"];
            $data["MAN"] = $row["SEX"];
        } else {
            $data["WOMAN_NAME"] = $row["NAME1"];
            $data["WOMAN"] = $row["SEX"];
        }
        $befItemCd = $row["ITEMCD"];
    }
    $result->free();

    //最後の種別
    $arg["data"][] = $data;
}

//権限チェック
function authCheck(&$arg)
{
    if (AUTHORITY != DEF_UPDATABLE) {
        $arg["jscript"] = "OnAuthError();";
    }
}
