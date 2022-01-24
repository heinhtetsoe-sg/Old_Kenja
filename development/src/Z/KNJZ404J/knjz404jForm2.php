<?php

require_once('for_php7.php');

class knjz404jForm2 {
    function main(&$model) {
        $objForm        = new form;
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjz404jindex.php", "", "edit");

        $db = Query::dbCheckOut();

        $nameRow= $db->getRow(knjz404jQuery::getSubclassName($model),DB_FETCHMODE_ASSOC);
        $arg['SUBCLASSNAME'] =$nameRow['LABEL'];

        //観点コード(MAX10)
        $view_key = array();
        $view_cnt = 0;
        $view_html = "";
        $view_html_no = array("1" => "①", "2" => "②", "3" => "③", "4" => "④", "5" => "⑤",
                              "6" => "⑥", "7" => "⑦", "8" => "⑧", "9" => "⑨", "10" => "⑩");
        $result = $db->query(knjz404jQuery::selectViewcdQuery($model));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $view_cnt++;
            if ($view_cnt > 10) break;   //MAX10
            $view_key[$view_cnt] = $row["VIEWCD"];
            //チップヘルプ
            $view_html .= "<th class=\"no_search\" height=\"25\" onMouseOver=\"ViewcdMousein(event, ".$view_cnt.")\" onMouseOut=\"ViewcdMouseout()\">".$view_html_no[$view_cnt]."</th>";
            knjCreateHidden($objForm, "VIEWCD".$view_cnt, $row["VIEWNAME"]);
        }
        for ($i = 0; $i < (10 -get_count($view_key)); $i++) $view_html .= "<th class=\"no_search\" height=\"25\">&nbsp;</th>";
        $arg["view_html"] = $view_html;

        if(!isset($nameRow['CNT'])) {
             $nameRow['CNT'] = 0;
        }
        $result = $db->query(knjz404jQuery::getRightList($model));
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $html = '';
            for ($i = 1; $i <= 10; $i++) {
                if ($nameRow['CNT']<$i) {
                    knjCreateHidden($objForm, "JVIEW_".$row['SCORE'].'_'.$i ,'');
                    $html.='<td style="text-align:center;background-color:#FFFFFF" width="8%"></td>';
                } else {
                    //$extra = "onblur=\"this.value=toInteger(this.value)\"";
                    $extra = "style=\"text-align:center\" onChange=\"this.style.background='#ccffcc'\" onblur=\"calc(this);\"";
                    $html.='<td style="text-align:center;background-color:#FFFFFF" width="8%">'.knjCreateTextBox($objForm, $row['JVIEW'.$i], "JVIEW_".$row['SCORE'].'_'.$i, 3, 1, $extra).'</td>';
                }
            }
            if ($row['SCORE']>0&&$row['SCORE']<=$model->dankai) {
                $rec[intval($row['SCORE'])-1]['VAL']='<tr><td class="no_search" style="text-align:center">'.$row['SCORE'].'</td>'.$html.'</tr>';
            }
        }
        for ($i = 0; $i < $model->dankai; $i++) {
            if (!isset($rec[$i])) {
                $rec[$i]['VAL'] = '<tr><td class="no_search" style="text-align:center">'.($i+1).'</td>';
                for ($j = 0; $j < 10; $j++) {
                    if ($nameRow['CNT'] <= $j) {
                        knjCreateHidden($objForm, "JVIEW_".($i+1).'_'.($j+1) ,'');
                        $rec[$i]['VAL'] .= '<td style="text-align:center;background-color:#FFFFFF" width="8%"></td>';
                    } else {
                        $extra = "style=\"text-align:center\" onChange=\"this.style.background='#ccffcc'\" onblur=\"calc(this);\"";
                        $rec[$i]['VAL'] .= '<td style="text-align:center;background-color:#FFFFFF" width="8%">'.knjCreateTextBox($objForm, '', "JVIEW_".($i+1).'_'.($j+1), 3, 1, $extra).'</td>';
                    }
                }
                $rec[$i]['VAL'] .= '</tr>';
            }
        }
        for ($i = 0; $i < $model->dankai; $i++) {
            $arg['data'][$i] = $rec[$model->dankai-1-$i];
        }

        //修正ボタン
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_udpate", "更 新", $extra);

        //削除ボタン
        $extra = "onclick=\"return btn_submit('delete');\"";
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);

        //クリアボタン
        $extra = "onclick=\"return btn_submit('reset')\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        if (VARS::get("cmd") != "edit" && !isset($model->warning)) {
            $arg["reload"]  = "window.open('knjz404jindex.php?cmd=list&ed=1','left_frame');";
        }
        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();

        View::toHTML($model, "knjz404jForm2.html", $arg); 
    }
}
?>
