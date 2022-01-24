<?php

require_once('for_php7.php');

class knjd137fSubForm3 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("subform3", "POST", "knjd137findex.php", "", "subform3");

        //DB接続
        $db = Query::dbCheckOut();

        //生徒情報
        $arg["NAME_SHOW"] = $model->schregno."  :  ".$model->name;

        //生徒情報
        $arg["NAME_SHOW"] = $model->schregno."  :  ".$model->name;

        if ($model->Properties["useQualifiedMst"] == '1') {
            $arg["TITLE"] = "取得日 ／ 区分 ／ 名称 ／ 略称 ／ 級・段位 ／ 備考";
        } else {
            $arg["TITLE"] = "取得日 ／ 区分 ／ 内容 ／ 備考";
        }

        //全てチェックボックス
        $extra = "onclick=\"checkAll()\"";
        $arg["ALL"] = knjCreateCheckBox($objForm, "ALL", "on", $extra);

        //部活動一覧
        $query = knjd137fQuery::getAward($model);
        $result = $db->query($query);
        $i = 0;
        $printField = array();
        if ($model->Properties["useQualifiedMst"] == '1') {
            $printField = array("REGDDATE", "CONDITION_DIV", "QUALIFIED_NAME", "QUALIFIED_ABBV", "RANK", "REMARK");
        } else {
            $printField = array("REGDDATE", "CONDITION_DIV", "CONTENTS", "REMARK");
        }
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row = str_replace("-","/",$row);

            $remark = "";
            foreach ($printField as $field) {
                if ($row[$field] == '') {
                    continue;
                }
                if ($remark != '') {
                    $remark .= "／";
                }
                $remark .= $row[$field];
            }
            $row["REMARK"] = $remark;

            //チェックボックス
            $extra = "";
            $row["RCHECK"] = knjCreateCheckBox($objForm, "RCHECK" . $i, "on", $extra);

            $arg["data"][] = $row;

            knjCreateHidden($objForm, "HIDDEN_RCHECK".$i, $row["REMARK"]);
            $i++;
        }


        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row = str_replace("-","/",$row);

            if ($model->Properties["useQualifiedMst"] == '1') {
                $row["REMARK"] = "{$row["REGDDATE"]}／{$row["CONDITION_DIV"]}／{$row["QUALIFIED_NAME"]}／{$row["QUALIFIED_ABBV"]}／{$row["RANK"]}／{$row["REMARK"]}";
            } else {
                $row["REMARK"] = "{$row["REGDDATE"]}／{$row["CONDITION_DIV"]}／{$row["CONTENTS"]}／{$row["REMARK"]}";
            }

            //チェックボックス
            $extra = "";
            $row["RCHECK"] = knjCreateCheckBox($objForm, "RCHECK" . $i, "on", $extra);

            $arg["data"][] = $row;

            knjCreateHidden($objForm, "HIDDEN_RCHECK".$i, $row["REMARK"]);
            $i++;
        }

        //反映ボタン
        $extra = "onclick=\"addRemark();\"";
        $arg["btn_reflect"] = knjCreateBtn($objForm, "btn_reflect", "反 映", $extra);
        //終了ボタン
        $extra = "onclick=\"return parent.closeit()\"";
        $arg["btn_back"] = KnjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd137fSubForm3.html", $arg);
    }
}
?>

