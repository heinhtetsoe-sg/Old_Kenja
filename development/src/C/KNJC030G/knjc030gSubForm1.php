<?php

require_once('for_php7.php');

class knjc030gSubForm1
{
    public function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("subform1", "POST", "knjc030gindex.php", "", "subform1");

        //DB接続
        $db = Query::dbCheckOut();

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

        $tempData = array();
        $result = $db->query(knjc030gQuery::selectQuery2($model, $model->selectdate));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $tempData[$row['PERIODCD']] = $row;
        }

        $week = array('日','月','火','水','木','金','土');
        $arg['date'] = date('n/d', strtotime($model->selectdate)).'('.$week[date('w', strtotime($model->selectdate))].')';
        $arg['dayvalue'] = knjCreateTextBox($objForm, $db->getOne(knjc030gQuery::getAttendDayDat($model, $model->selectdate)), "DI_REMARK", 40, 20, '');
        foreach ($model->periodArray as $i => $value) {
            $row['title'] = $value['LABEL'];
            $disabled = (isset($tempData[$i]) && $tempData[$i]['DI_CD_FLAG'] == '1') ? '' :'disabled="disabled"';
            $row['value'] = knjCreateTextBox($objForm, $tempData[$i]['DI_REMARK'], "DI_REMARK_".$i, 40, 20, $disabled);
            $arg['data'][] = $row;
        }
        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjc030gSubForm1.html", $arg);
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model)
{
    //更新ボタン
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", "onclick=\"return btn_submit('updateSubform');\"");
    //戻るボタン
    $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", "onclick=\"parent.btn_submit('edit');\"");
}

//hidden作成
function makeHidden(&$objForm, $model)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
}
