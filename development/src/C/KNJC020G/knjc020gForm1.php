<?php

require_once('for_php7.php');

class knjc020gForm1
{
    public function main(&$model)
    {
        //フォーム作成
        $objForm = new form();
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjc020gindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        $opt = array(array('label'=>'','value'=>''));
        $result = $db->query(knjc020gQuery::getChair($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row['LABEL'],
                           "value" => $row["VALUE"]);
        }
        $arg['CHAIRCD'] = knjCreateCombo($objForm, 'CHAIRCD', $model->chairCd, $opt, ' onchange="btn_submit(\'edit\');"', '');

        $arg['TODAY'] = knjCreateBtn($objForm, "btn_today", "本 日", ' onclick="btn_submit(\'today\');"');
        $arg["START_DATE"] = View::popUpCalendar2($objForm, "START_DATE", str_replace("-", "/", $model->startDate), '', 'btn_submit(\'edit\');');
        $arg["END_DATE"] = str_replace("-", "/", $model->endDate);
        $arg['READ'] = knjCreateBtn($objForm, "btn_read", "読 込", ' onclick="btn_submit(\'edit\');"');
        $arg['BEFORE_WEEK'] = knjCreateBtn($objForm, "btn_read", "前の2週間", ' onclick="btn_submit(\'before\');"');
        $arg['NEXT_WEEK'] = knjCreateBtn($objForm, "btn_read", "次の2週間", ' onclick="btn_submit(\'next\');"');

        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

        $extra = "onclick=\"return btn_submit('clear');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        if (isset($model->chairCd)) {
            $result = $db->query(knjc020gQuery::selectQuery2($model));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $tempData[$row['EXECUTEDATE']][$row['PERIODCD']][] = $row;
            }
            $result->free();
            $result = $db->query(knjc020gQuery::selectQuery($model));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $arg['data'][] = $row;
            }
            $result->free();
        }
        $opt = array();
        $opt[] = array("label" => "", "value" => "");
        $opt[] = array("label" => "出席", "value" => "SHUSSEKI");
        $result = $db->query(knjc020gQuery::getDiCd($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }
        $result->free();

        $week = array('日','月','火','水','木','金','土');
        $schregnos = '';
        $dayPeriods = '';
        $sep = '';
        $perisep = '';
        $idx = 0;
        for ($i = 0; $i < get_count($arg['data']); $i++) {
            $inputData = $arg['data'][$i];
            $schregnos.= $sep.$inputData['SCHREGNO'];
            $sep = ',';
            $text1='';
            $text2='';
            $cnt = 0;
            if (isset($tempData)) {
                foreach ($tempData as $day => $value) {
                    $dispDay = date('n/d', strtotime($day)).'('.$week[date('w', strtotime($day))].')';
                    foreach ($value as $period => $row) {
                        if ($i==0) {
                            $arg['TITLE'][$cnt]['DATE'] = $dispDay;
                            $arg['TITLE'][$cnt]['BUTTON'] = knjCreateBtn($objForm, "btn_update", "出席", "onclick=\"syukketu('".$cnt."')\"");
                            $key="UPDATE_CHECK_" . $cnt;
                            $extra =" id='{$key}'";
                            $check= knjCreateCheckBox($objForm, $key, "1", $extra, "")."<label for='{$key}'>更新</label>";
                            $arg['TITLE'][$cnt]['TITLE'] = "<th nowrap>".$row[0]['PERIODNAME'].'</th><th nowrap>'.$check."</th>";
                            $dayPeriods .= $perisep . $day.'_'.$period;
                            $perisep = ',';
                        }
                        $diMark1 = '';
                        $diMark2 = '';
                        $diCd1='';
                        $diCd2='';
                        $kesseki1 = '';
                        $kesseki2 = '';
                        $class = 'setColor4';
                        for ($j = 0; $j < get_count($row); $j++) {
                            if ($row[$j]['SCHREGNO'] == $inputData['SCHREGNO']) {
                                $diMark1 = $row[$j]['DI_MARK1'];
                                $diMark2 = $row[$j]['DI_MARK2'];
                                $diCd1 = $row[$j]['DI_CD1'];        //講座担当欠課データ
                                $diCd2 = $row[$j]['DI_CD2'];        //担任欠課データ
                                $kesseki1 = $row[$j]['KESSEKI1'];   //講座担当欠席フラグ
                                $kesseki2 = $row[$j]['KESSEKI2'];   //担任欠席フラグ
                                $diRemark = $row[$j]['DI_REMARK'];
                                if ($diCd1 != '' && $diCd2 != '') {
                                    $class = "setColor1";
                                }
                                if ($kesseki1 == '1') {
                                    $class = "setColor2";
                                }
                                if ($diCd1 == '' && $diCd2 != '') {
                                    $class = "setColor3";
                                }
                                if ($kesseki1 != '1' && $diCd2 == '') {
                                    $class = "setColor4";
                                }
                            }
                        }
                        $text1.='<td colspan="2" width="100" class="syukketu '.$class.'">'.knjCreateCombo($objForm, 'syukketu_'.$cnt.'_'.$i, $diCd1, $opt, 'onchange="syukketu2(\''.$cnt.'\')"', '').'</td>';
                        $text2.='<td colspan="2" width="100" class="tannnin" onMouseOver="ViewcdMousein(event, \''.$idx.'\')" onMouseOut="ViewcdMouseout()">'.$diMark2.'</td>';
                        knjCreateHidden($objForm, "TIP_".$idx, $diRemark);
                        $cnt++;
                        $idx++;
                    }
                }
            }
            $arg['data'][$i]['TEXT1'] = $text1;
            $arg['data'][$i]['TEXT2'] = $text2;
            $arg['width'] = $cnt * 100;
        }

        if (is_null($arg["TITLE"])) {
            $arg['TITLE'][0]['DATE']='';
            $arg['TITLE'][0]['TITLE']='<th nowrap></th>';
            for ($i = 0; $i < get_count($arg['data']); $i++) {
                $arg['data'][$i]['TEXT1'] = '<td class="syukketu"></td>';
                $arg['data'][$i]['TEXT2'] = '<td class="tannnin"></td>';
            }
        }
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "schregnos", $schregnos);
        knjCreateHidden($objForm, "dayPeriods", $dayPeriods);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjc020gForm1.html", $arg);
    }
}
