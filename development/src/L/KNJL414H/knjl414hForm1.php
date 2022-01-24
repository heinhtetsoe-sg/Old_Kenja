<?php
class knjl414hForm1
{
    public function main(&$model)
    {
        $objForm = new form();
        $arg["start"]   = $objForm->get_start("list", "POST", "knjl414hindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["TOP"]["ENTEXAMYEAR"] = $model->year;

        $opt = array();
        $result = $db->query(knjl414hQuery::getNameMst($model, 'L003'));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($model->applicantdiv == '' && $row["NAMESPARE2"] == '1') {
                $value= $row["VALUE"];
            }
        }
        if ($model->applicantdiv == '') {
            $model->applicantdiv = $value;
        }
        $extra = " onchange=\"btn_submit('edit');\"";
        $size = "";
        $arg["TOP"]['APPLICANTDIV'] = knjCreateCombo($objForm, 'APPLICANTDIV', $model->applicantdiv, $opt, $extra, $size);

        $result->free();

        if ($model->applicantdiv == '1') {
            $arg['applicantdiv_flg3'] = '1';
        } elseif ($model->applicantdiv == '2') {
            $arg['applicantdiv_flg1'] = '1';
        } else {
            $arg['applicantdiv_flg1'] = '1';
            $arg['applicantdiv_flg3'] = '1';
        }

        if ($arg['applicantdiv_flg1'] != '1' && $model->radio == '1') {
            $model->radio = '2';
        }
        if ($arg['applicantdiv_flg3'] != '1' && $model->radio == '3') {
            $model->radio = '2';
        }

        //表示順序ラジオボタン 1:氏名（50音順） 2:登録順
        $opt = array(1, 2, 3);
        $extra = array("id=\"RADIO1\" onclick=\"btn_submit('edit');\"", "id=\"RADIO2\" onclick=\"btn_submit('edit');\"", "id=\"RADIO3\" onclick=\"btn_submit('edit');\"");
        $radioArray = knjCreateRadio($objForm, "RADIO", $model->radio, $extra, $opt, count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["TOP"][$key] = $val;
        }
        if ($model->radio == '1') {
            $arg['radio_flg1'] = '1';
        } elseif ($model->radio == '2') {
            $arg['radio_flg2'] = '1';
        } elseif ($model->radio == '3') {
            $arg['radio_flg3'] = '1';
        }

        $opt2 = array(array('label'=>'','value'=>''));
        $result = $db->query(knjl414hQuery::getSettingMst($model, 'L100'));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt2[] = array('label' => $row["LABEL"],
                            'value' => $row["VALUE"]);
        }
        $extra = "";

        $result->free();

        $optChkArry = array();
        $optChkStr = "0#0:";
        $sep = ",";
        $query = knjl414hQuery::getSettingMstl101($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $optChkArry[$row["VALUE"]] = $row["NAME2"]."#".$row["LABEL"];
            $optChkStr .= $sep.$row["NAME2"]."#".$row["LABEL"];
        }
        $result->free();
        $examnos='';
        $sep = "";
        $query = knjl414hQuery::getList($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //レコードを連想配列のまま配列$arg[data]に追加していく。
            array_walk($row, "htmlspecialchars_array");
            if ($model->radio == '1') {
                $extra = "style=\"text-align:right\" onblur=\"chkInputTxt(this, '1')\";";
                $row['CD2REMARK1'] = knjCreateTextBox($objForm, $row["CD2REMARK1"], "CD2REMARK1[]", 10, null, $extra);
                $row['CD2REMARK2'] = knjCreateTextBox($objForm, $row["CD2REMARK2"], "CD2REMARK2[]", 10, null, $extra);
                $row['CD2REMARK3'] = knjCreateTextBox($objForm, $row["CD2REMARK3"], "CD2REMARK3[]", 10, null, $extra);
                $extra="onclick='checkClick(this);'".(($row["CD2REMARK4"] == '1') ? " checked" : "");
                $val = ($row["CD2REMARK4"] == '1') ? "1" : "";
                $row['CD2REMARK4'] = knjCreateCheckBox($objForm, "CD2REMARK4_".$row['EXAMNO'], "1", $extra, "")."<input type='hidden' name='CD2REMARK4[]' value='".$val."' id='CD2REMARK4_".$row['EXAMNO']."_hidden'>";
                $extra="onclick='checkClick(this);'".(($row["CD2REMARK7"] == '1') ? " checked" : "");
                $val = ($row["CD2REMARK7"] == '1') ? "1" : "";
                $row['CD2REMARK7'] = knjCreateCheckBox($objForm, "CD2REMARK7_".$row['EXAMNO'], "1", $extra, "")."<input type='hidden' name='CD2REMARK7[]' value='".$val."' id='CD2REMARK7_".$row['EXAMNO']."_hidden'>";
            } elseif ($model->radio == '2') {
                $extra = "onchange=\"chgFlg()\";";
                $row['CD3REMARK1'] = knjCreateCombo($objForm, 'CD3REMARK1[]', $row["CD3REMARK1"], $opt2, $extra, null);
                $extra = "style=\"text-align:right\" onblur=\"chkInputTxt(this, '2')\";";
                $row['CD3REMARK2'] = knjCreateTextBox($objForm, $row["CD3REMARK2"], "CD3REMARK2[]", 10, null, $extra);
            } elseif ($model->radio == '3') {
                $extra = "onblur=\"chkMax(this, '{$row['EXAMNO']}')\";";
                $exId = " id=\"CD4REMARK1-{$row['EXAMNO']}\" style=\"text-align:right\" ";
                $row['CD4REMARK1'] = knjCreateTextBox($objForm, $row["CD4REMARK1"], "CD4REMARK1[]", 10, null, $exId.$extra);
                $exId = " id=\"CD4REMARK2-{$row['EXAMNO']}\" style=\"text-align:right\" ";
                $row['CD4REMARK2'] = knjCreateTextBox($objForm, $row["CD4REMARK2"], "CD4REMARK2[]", 10, null, $exId.$extra);
                $exId = " id=\"CD4REMARK3-{$row['EXAMNO']}\" style=\"text-align:right\" ";
                $row['CD4REMARK3'] = knjCreateTextBox($objForm, $row["CD4REMARK3"], "CD4REMARK3[]", 10, null, $exId.$extra);
                $exId = " id=\"CD4REMARK4-{$row['EXAMNO']}\" ";
                $row['CD4REMARK4'] = knjCreateTextBox($objForm, $row["CD4REMARK4"], "CD4REMARK4[]", 10, null, $exId."disabled='disabled'");
                $exId = " id=\"CD4REMARKX-{$row['EXAMNO']}\" ";
                $row['CD4REMARKX'] = "<label id=\"CD4REMARKX-{$row['EXAMNO']}\">".$row["CD4REMARKX"]."</label>";
            }
            $examnos.=$sep.$row['EXAMNO'];
            $sep=',';
            $arg["data"][] = $row;
        }
        $result->free();


        Query::dbCheckIn($db);

        //開始受験番号テキストボックス
        $extra = " onblur=\"this.value=toInteger(this.value);\"";
        $arg["TOP"]["S_RECEPTNO"] = knjCreateTextBox($objForm, $model->s_receptOrg, "S_RECEPTNO", 7, 7, $extra);

        //次へボタン
        $extra = "onclick=\"return btn_submit('next');\"";
        $arg["BUTTON"]["NEXT"] = knjCreateBtn($objForm, "btn_next", ">>", $extra);

        //前へボタン
        $extra = "onclick=\"return btn_submit('back');\"";
        $arg["BUTTON"]["BACK"] = knjCreateBtn($objForm, "btn_back", "<<", $extra);

        //修正ボタン
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["BUTTON"]["UPDATE"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

        //削除ボタン
        $extra = "onclick=\"return btn_submit('delete');\"";
        $arg["BUTTON"]["DELETE"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);

        //クリアボタン
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["BUTTON"]["RESET"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["BUTTON"]["END"] = knjCreateBtn($objForm, "btn_back", "終 了", $extra);


        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "examnos", $examnos);
        knjCreateHidden($objForm, "optChkArry", $optChkStr);
        knjCreateHidden($objForm, "EditedFlg", "0");
        knjCreateHidden($objForm, "DispMode", $model->radio);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl414hForm1.html", $arg);
    }
}
