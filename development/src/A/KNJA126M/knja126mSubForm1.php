<?php

require_once('for_php7.php');

class knja126mSubForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("subform1", "POST", "knja126mindex.php", "", "subform1");

        //DB接続
        $db = Query::dbCheckOut();

        //生徒情報
        $arg["SCHREGNO"] = $model->schregno;
        $arg["NAME"]     = $model->name;

        //項目名
        if ($model->Properties["useQualifiedMst"] == '1') {
            $arg["TITLE"] = "取得日 ／ 区分 ／ 名称 ／ 級・段位 ／ 備考";
        } else {
            $arg["TITLE"] = "取得日 ／ 区分 ／ 内容 ／ 備考";
        }

        //学籍資格データよりデータを取得
        if($model->schregno) {
            $result = $db->query(knja126mQuery::getAward($model));
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $row["REGDDATE"]  = str_replace("-","/",$row["REGDDATE"]);

                if ($model->Properties["useQualifiedMst"] == '1') {
                    $arg["data"][] = array("LIST" => "{$row["REGDDATE"]}／{$row["CONDITION_DIV"]}／{$row["QUALIFIED_NAME"]}／{$row["RANK"]}／{$row["REMARK"]}");
                } else {
                    $arg["data"][] = array("LIST" => "{$row["REGDDATE"]}／{$row["CONDITION_DIV"]}／{$row["CONTENTS"]}／{$row["REMARK"]}");
                }
            }
        }

        //戻るボタン
        $extra = "onclick=\"return parent.closeit()\"";
        $arg["btn_back"] = KnjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knja126mSubForm1.html", $arg);
    }
}
?>
