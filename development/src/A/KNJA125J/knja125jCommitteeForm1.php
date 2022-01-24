<?php
class knja125jCommitteeForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("committeeForm1", "POST", "knja125jindex.php", "", "committeeForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //生徒情報
        $arg["NAME_SHOW"] = $model->schregno."  :  ".$model->name;

        //年度表示
        $arg["YEAR"] = $model->exp_year."年度";

        //全てチェックボックス
        $extra = "onclick=\"checkAll()\"";
        $arg["ALL"] = knjCreateCheckBox($objForm, "ALL", "on", $extra);

        //委員会一覧
        $query = knja125jQuery::getCommittee($model);
        $result = $db->query($query);
        $i = 0;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row = str_replace("-","/",$row);
            $exename = "";
            if ($row["EXECUTIVE_NAME"] != '') {
                $exename = " ／ ".$row["EXECUTIVE_NAME"];
            }
            $row["COMMITTEENAME_CHARGENAME"] = $row["SEQ"].":".$row["COMMITTEENAME"].$exename;

            //チェックボックス
            $extra = "";
            $row["RCHECK"] = knjCreateCheckBox($objForm, "RCHECK" . $i, "on", $extra);

            $arg["data"][] = $row;

            knjCreateHidden($objForm, "HIDDEN_RCHECK".$i, $row["COMMITTEENAME"].$exename."　".$row["DETAIL_REMARK"]);
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
        View::toHTML($model, "knja125jCommitteeForm1.html", $arg);
    }
}
?>

