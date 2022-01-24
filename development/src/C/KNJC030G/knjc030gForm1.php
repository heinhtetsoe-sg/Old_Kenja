<?php

require_once('for_php7.php');

class knjc030gForm1
{
    public function main(&$model)
    {
        $objForm = new form();
        $arg["start"] = $objForm->get_start("edit", "POST", "knjc030gindex.php", "", "edit");
        $db = Query::dbCheckOut();
        //警告メッセージを表示しない場合
        if ((isset($model->schregno) && !isset($model->warning)) || !isset($model->schregno)) {
        } else {
            $row =& $model->field;
        }

        //開始日付
        $extra = "";
        $arg["SELECT_DATE"] = View::popUpCalendar2($objForm, "SELECT_DATE", str_replace("-", "/", $model->startDate), '', "btn_submit('edit');");
        $arg['END_DATE'] = str_replace("-", "/", $model->endDate);

        $arg["SCHREGNO"] = $model->schregno;
        $arg["NAME"]     = $model->name;
        $arg["HR_NAME"]  = $db->getOne(knjc030gQuery::getHrName($model));
        $arg['ATTENDNO'] = $model->attendno;

        $ext = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $ext);

        $ext = "onclick=\"return btn_submit('clear');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $ext);

        $ext = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $ext);

        //校時
        $result = $db->query(knjc030gQuery::getPeriod($model, 'B001'));
        /*
        * periodArray
        * LABEL:校時名称
        * PERI_YOMIKAE:連番(校時コードにアルファベットがある為)
        */
        $model->periodArray = array();
        $model->maxPeri = 0;
        $model->periYomikae = array();
        $title = array();
        $periCnt = 1;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $model->periodArray[$row["VALUE"]]["LABEL"] = $row["LABEL"];
            $model->periodArray[$row["VALUE"]]["PERI_YOMIKAE"] = $periCnt;
            $model->maxPeri = $periCnt;
            $model->periYomikae[$periCnt] = $row["VALUE"];
            $title[] = $row["LABEL"];
            $periCnt++;
        }
        $result->free();

        $arg["TITLE"] = $title;

        $opt = array();
        $opt[] = array("label" => "", "value" => "");
        $opt[] = array("label" => "出席", "value" => "SHUSSEKI");
        $result = $db->query(knjc030gQuery::getDiCd($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }
        $result->free();

        for ($i = 0; $i < 14; $i++) {
            $day = date('Y-m-d', strtotime($model->startDate . ' +' . $i . ' day'));
            $tempData[$day] = array();
            $dayList[$day] = $i;
        }

        $result = $db->query(knjc030gQuery::selectQuery($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $tempData[$row['EXECUTEDATE']][$row['PERIODCD']][$row['CHAIRCD']] = $row;
        }
        $week = array('日','月','火','水','木','金','土');
        $dayIdx = 0;
        foreach ($tempData as $rowDate => $value) {
            $tagData = '';
            $flg = false;
            foreach ($model->periodArray as $i => $dumy) {
                $text = '';
                if (isset($value[$i])) {
                    $chairCd = array_keys($value[$i])[0];
                    $text = $chairCd . ' ' . $value[$i][$chairCd]['CHAIRABBV'] . '<br>' . $value[$i][$chairCd]['STAFFNAME_SHOW'];
                    $text .= knjCreateTextBox($objForm, $value[$i][$chairCd]['DI_CD2_NAME'], "DUMY", 4, 4, ' disabled=""');
                    $key = $dayList[$rowDate] . '_' . $model->periodArray[$i]["PERI_YOMIKAE"];
                    $text .= '<br>' . knjCreateCombo($objForm, 'syukketu_'.$key, $value[$i][$chairCd]['DI_CD'], $opt, '', '');
                    $DI1 = $value[$i][$chairCd]['DI_CD'];         //担任欠課データ
                    $DI2 = $value[$i][$chairCd]['DI_CD2'];        //講座担当欠課データ
                    $kesseki1 = $value[$i][$chairCd]['KESSEKI1']; //担任欠席フラグ
                    $kesseki2 = $value[$i][$chairCd]['KESSEKI2']; //講座担当欠席フラグ
                    $flg = true;
                    if ($kesseki1 == '1' && $kesseki2 == '1') {
                        $class = 'active';
                    } elseif (($kesseki2 != '1' && $DI2 != '') && ($DI1 == '')) {
                        $class = 'active';
                    } elseif ($kesseki2 == '1' && $DI1 == '') {
                        $class = 'setColor1';
                    } elseif ($DI2 == '' && $DI1 != '') {
                        $class = 'setColor2';
                    } elseif ($DI2 != $DI1) {
                        $class = 'setColor2';
                    } else {
                        $class = 'active';
                    }
                } else {
                    $class = 'nonActive';
                }
                $tagData .= "<td class='".$class."'>".$text."</td>";
            }
            $headData   = "<th class='no_search' style='text-align:center' onMouseOver=\"ViewcdMousein(event, '".$dayIdx."')\" onMouseOut=\"ViewcdMouseout()\">";
            $headData .= date('n/d', strtotime($rowDate)).'('.$week[date('w', strtotime($rowDate))].')';
            if ($flg) {
                $headData .= '<br>' . knjCreateCombo($objForm, 'row_syukketu_'.$dayIdx, '', $opt, ' onchange="rowSyukketu('.$dayIdx.')"', '');
                $headData .= '<br>'.knjCreateBtn($objForm, "btn_syusseki", "備考", ' onclick="loadwindow(\'knjc030gindex.php?cmd=subform1&selectdate='.$rowDate.'\', 0, 0, 500, 600);"');

                $tempData2 = array();
                $result = $db->query(knjc030gQuery::selectQuery2($model, $rowDate));
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $tempData2[$row['PERIODCD']] = $row;
                }
                $tipdata = '';
                $dayvalue = $db->getOne(knjc030gQuery::getAttendDayDat($model, $rowDate));
                if ($dayvalue != '') {
                    $tipdata.="<tr><td>日</td><td>{$dayvalue}</td></tr>";
                }
                foreach ($model->periodArray as $i => $dumy) {
                    if (isset($tempData2[$i]) && $tempData2[$i]['DI_REMARK'] != '') {
                        $tipdata.="<tr><td>{$model->periodArray[$i]['LABEL']}</td><td>{$tempData2[$i]['DI_REMARK']}</td></tr>";
                    }
                }
                $arg['MENU'][]['DATA'] = '<div id="MENU_'.$dayIdx.'" style="display:none"><table border="1" cellspacing="0" cellpadding="3" style="border-collapse: collapse">'.$tipdata.'</table></div>';
            }
            $headData .= "</th>";
            $arg['DATA'][] = '<tr>' . $headData . $tagData . '</tr>';
            $dayIdx++;
        }
        knjCreateHidden($objForm, "cmd", "edit");
        knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
        knjCreateHidden($objForm, "MAXPERI", $model->maxPeri);
        Query::dbCheckIn($db);
        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjc030gForm1.html", $arg);
    }
}
