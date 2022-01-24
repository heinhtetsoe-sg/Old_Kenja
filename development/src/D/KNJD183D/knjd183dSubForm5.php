<?php

require_once('for_php7.php');


class knjd183dSubForm5
{
    public function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("list", "POST", "knjd183dindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //学籍番号・生徒氏名表示
        $arg["NAME_SHOW"] = $model->schregno."  :  ".$model->name;

        //ALLチェック
        $extra = " id=\"CHECKALL\" onClick=\"check_all(this); OptionUse(this)\"";
        $arg["CHECKALL"] = knjCreateCheckBox($objForm, "CHECKALL", "", $extra, "");

        //対象項目
        if ($model->Properties["useQualifiedMst"] == '1') {
            $itemArray = array("QUALIFIED", "RANK");
            $arg["useQualifiedMst"] = 1;
        } else {
            $itemArray = array("CONTENTS", "REMARK");
            $arg["NOTuseQualifiedMst"] = 1;
        }
        knjCreateHidden($objForm, "item", implode(',', $itemArray));

        //検定リスト
        $counter = 0;
        if ($model->schregno) {
            $result = $db->query(knjd183dQuery::getAward($model));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $row["REGDDATE"] = str_replace("-", "/", $row["REGDDATE"]);

                $row["RANK_SHOW"]  = $row["RANK"];
                $row["RANK_SHOW"] .= ((strlen($row["RANK_SHOW"]) > 0) ? " " : "").$row["REMARK"];

                $row["CONTENTS_SHOW"] = $row["CONTENTS"];
                $row["REMARK_SHOW"] = $row["REMARK"];

                //選択チェックボックス
                $value = $counter;
                $extra = "onclick=\"OptionUse(this);\"";
                $row["CHECKED"] = knjCreateCheckBox($objForm, "CHECKED", $value, $extra, "1");

                foreach ($itemArray as $key) {
                    knjCreateHidden($objForm, $key.":".$counter, $row[$key."_SHOW"]);
                }

                $arg["data"][] = $row;
                $counter++;
            }
        }

        foreach ($itemArray as $key) {
            //対象項目チェックボックス
            $extra  = ($counter > 0) ? "" : "disabled";
            $extra .= " checked id=\"CHECK_{$key}\" onclick=\"return OptionUse(this);\"";
            $arg["CHECK_".$key] = knjCreateCheckBox($objForm, "CHECK_".$key, $key, $extra, "");
        }

        //取込ボタン
        $extra = "disabled style=\"color:#1E90FF;font:bold\" onclick=\"return dataPositionSet('SHUTOKUSIKAKU');\"";
        $arg["btn_torikomi"] = knjCreateBtn($objForm, "btn_torikomi", "取 込", $extra);

        //終了ボタン
        $extra = "onclick=\"return parent.closeit()\"";
        $arg["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"] = $objForm->get_finish();

        if (VARS::get("cmd") == "right_list") {
            $arg["reload"] = "parent.edit_frame.location.href='knjd183dindex.php?cmd=edit'";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd183dSubForm5.html", $arg);
    }
}
