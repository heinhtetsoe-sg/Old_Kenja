<?php

require_once('for_php7.php');

class knjx_rishu_kojinForm1
{
    function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjx_rishu_kojinindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //テーブル情報件数確認
        $model->schregSend_data = "";
        $model->schregSend_data = $db->getOne(knjx_rishu_kojinQuery::countSchregSendAddress($model->schregno, '1'));

        //データ有無の確認
        //警告メッセージを表示しない場合
        if ((isset($model->schregno) && !isset($model->warning)) || !isset($model->schregno)){
            $Row = $db->getRow(knjx_rishu_kojinQuery::getRow($model->schregno, '1'), DB_FETCHMODE_ASSOC);
            //データセット用に取得
            if ($model->getSearch_div === '1') {
                $RowSet = $db->getRow(knjx_rishu_kojinQuery::getRowFresh($model->schregno), DB_FETCHMODE_ASSOC);
            } else if ($model->getSearch_div === '2') {
                $RowSet = $db->getRow(knjx_rishu_kojinQuery::getRowAddress($model->schregno), DB_FETCHMODE_ASSOC);
            }
            //SCHREG_BASE_DETAIL_MST情報
            if ($model->cmd !== 'check') {
                $RowDetail = $db->getRow(knjx_rishu_kojinQuery::getDetailRow($model->schregno, '004'), DB_FETCHMODE_ASSOC);
            } else {
                $RowDetail =& $model->field;
            }
            //SCHREG_BASE_YEAR_DETAIL_MST情報
            $RowYearDetail = $db->getRow(knjx_rishu_kojinQuery::getYearDetailRow($model, '002'), DB_FETCHMODE_ASSOC);
            
            $arg["NOT_WARNING"] = 1;
        } else {
            $Row =& $model->field;
            $RowSet =& $model->field;
            $RowDetail =& $model->field;
            $RowYearDetail =& $model->field;
        }

        //生徒情報
        $arg["SCHREGNO"] = $model->schregno;
        $arg["SET_NAME"] = $model->name;
        
        //氏名
        $extra = "";
        if ($Row["NAME"] == "" && $RowSet["NAME"] != "") {
            $arg["data"]["NAME"] = knjCreateTextBox($objForm, $RowSet["NAME"], "NAME", 30, 60, $extra);
        } else {
            $arg["data"]["NAME"] = knjCreateTextBox($objForm, $Row["NAME"], "NAME", 30, 60, $extra);
        }
        
        //郵便番号
        if ($Row["ADDR1"] == "" && $RowSet["ADDR1"] != "") {
            $arg["data"]["ZIPCD"] = View::popUpZipCode($objForm, "ZIPCD", $RowSet["ZIPCD"],"ADDR1");
        } else {
            $arg["data"]["ZIPCD"] = View::popUpZipCode($objForm, "ZIPCD", $Row["ZIPCD"],"ADDR1");
        }

        //住所1
        $extra = "";
        if ($Row["ADDR1"] == "" && $RowSet["ADDR1"] != "") {
            $arg["data"]["ADDR1"] = knjCreateTextBox($objForm, $RowSet["ADDR1"], "ADDR1", 50, 75, $extra);
        } else {
            $arg["data"]["ADDR1"] = knjCreateTextBox($objForm, $Row["ADDR1"], "ADDR1", 50, 75, $extra);
        }
        
        //住所2
        $extra = "";
        if ($Row["ADDR1"] == "" && $RowSet["ADDR1"] != "") {
            $arg["data"]["ADDR2"] = knjCreateTextBox($objForm, $RowSet["ADDR2"], "ADDR2", 50, 75, $extra);
        } else {
            $arg["data"]["ADDR2"] = knjCreateTextBox($objForm, $Row["ADDR2"], "ADDR2", 50, 75, $extra);
        }
        
        //方書きを住所1とする
        $extra = $Row["ADDR_FLG"] == "1" ? " checked " : "";
        $arg["data"]["ADDR_FLG"] = knjCreateCheckBox($objForm, "ADDR_FLG", "1", $extra);
        
        //電話番号1
        $extra = "";
        if ($Row["TELNO"] == "" && $RowSet["TELNO"] != "") {
            $arg["data"]["TELNO"] = knjCreateTextBox($objForm, $RowSet["TELNO"], "TELNO", 16, 14, $extra);
        } else {
            $arg["data"]["TELNO"] = knjCreateTextBox($objForm, $Row["TELNO"], "TELNO", 16, 14, $extra);
        }
        
        //電話番号2
        $extra = "" . $schreg_disabled;
        $arg["data"]["TELNO2"] = knjCreateTextBox($objForm, $Row["TELNO2"], "TELNO2", 16, 14, $extra);
        
        //職業
        $opt = array();
        $opt[] = array("label" => "","value" => "");
        $query = knjx_rishu_kojinQuery::getNameMst('H202');
        $value_flg = false;
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }
        $extra = "";
        $arg["data"]["JOBCD"] = knjCreateCombo($objForm, "JOBCD", $Row["JOBCD"], $opt, $extra, 1);
        
        //地区コード
        $opt = array();
        $opt[] = array("label" => "","value" => "");
        $query = knjx_rishu_kojinQuery::getNameMst('A020');
        $value_flg = false;
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }
        $extra = "";
        $arg["data"]["AREACD"] = knjCreateCombo($objForm, "AREACD", $Row["AREACD"], $opt, $extra, 1);

        //前籍校入力完了
        $readonly = "onclick=\"return btn_submit('check');\"";
        if ($RowDetail["BASE_REMARK5"] == "1") {
            $extra = "checked='checked' ";
        } else {
            $extra = "";
        }
        $arg["data"]["BASE_REMARK5"] = knjCreateCheckBox($objForm, "BASE_REMARK5", "1", $extra.$readonly);

        //査定単位数
        if ($RowDetail["BASE_REMARK5"] == "1") {
            $extra = "onkeypress=\"btn_keypress();\" onkeydown=\"btn_onkeydown();\" style=\"height:18px;background-color:#D0D0D0;\" readonly";
        } else {
            $extra = "onblur=\"this.value=toInteger(this.value);\"";

        }
        $arg["data"]["BASE_REMARK1"] = knjCreateTextBox($objForm, $RowDetail["BASE_REMARK1"], "BASE_REMARK1", 2, 2, $extra);

        //特別活動時数
        $opt = array();
        $opt[] = array("label" => "","value" => "");
        $query = knjx_rishu_kojinQuery::getNameMst('M013');
        $value_flg = false;
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }
        $extra = "";
        $arg["data"]["BASE_REMARK2"] = knjCreateCombo($objForm, "BASE_REMARK2", $RowDetail["BASE_REMARK2"], $opt, $extra, 1);

        //スクーリング対象年度
        $arg["data"]["YEAR"] = ($model->exp_year) ? $model->exp_year."年度" : "";

        //スクーリング日
        $opt = array();
        $opt[] = array("label" => "","value" => "");
        $query = knjx_rishu_kojinQuery::getNameMst('M014');
        $value_flg = false;
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }
        $extra = "";
        $arg["data"]["SCHOOLING_DATE"] = knjCreateCombo($objForm, "SCHOOLING_DATE", $RowYearDetail["SCHOOLING_DATE"], $opt, $extra, 1);

        if ($model->schregSend_data != "0") {
            //更新ボタン
            $extra = "onclick=\"return btn_submit('update');\"";
            $arg["button"]["btn_update"] = KnjCreateBtn($objForm, "btn_update", "更 新", $extra);
        } else {
            //更新ボタン
            $extra = "onclick=\"return btn_submit('update');\"";
            $arg["button"]["btn_update"] = KnjCreateBtn($objForm, "btn_update", "追 加", $extra);
        }

        //取消ボタン
        $extra = "onclick=\"return btn_submit('clear');\"";
        $arg["button"]["btn_reset"] = KnjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        //終了ボタン
        $extra = "onclick=\"closeMethod();\"";
        $arg["button"]["btn_end"] = KnjCreateBtn($objForm, "btn_end", "戻 る", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "SCHREGNO", $model->schregno);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjx_rishu_kojinForm1.html", $arg);
    }
}
?>
