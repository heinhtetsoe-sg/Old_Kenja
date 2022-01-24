<?php

require_once('for_php7.php');


class knjg060aSubForm1
{
    function main(&$model)
    {
        $objForm = new form;
        $arg = array();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjg060aSubForm1", "POST", "knjg060aindex.php", "", "knjg060aSubForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //名称設定
        $arg["SCHREGNO"] = $model->subSchregno;
        $arg["NAME"] = $db->getOne(knjg060aQuery::getName($model->subSchregno));

        //通学データ
        $query = knjg060aQuery::getSchreg_envir_dat($model);
        $studentTugaku = $db->getRow($query, DB_FETCHMODE_ASSOC);
        for ($tugakuCnt = 1; $tugakuCnt <= 7; $tugakuCnt++) {
            if ($studentTugaku["JOSYA_".$tugakuCnt] || $studentTugaku["ROSEN_".$tugakuCnt] || $studentTugaku["GESYA_".$tugakuCnt]) {
                $arg["data"]["JOSYA_FLG_".$tugakuCnt] = 1;
            }
            if ($studentTugaku["FLG_".$tugakuCnt] == 2) {
                $josya_mei = $studentTugaku["JOSYA_".$tugakuCnt];
                $rosen_mei = $studentTugaku["GESYA_".$tugakuCnt];
                $gesya_mei = $studentTugaku["ROSEN_".$tugakuCnt];
            } else {
                $query = knjg060aQuery::getStationName($studentTugaku["JOSYA_".$tugakuCnt]);
                list($josya_mei, $rosen_mei) = $db->getRow($query);
                $query = knjg060aQuery::getStationName($studentTugaku["GESYA_".$tugakuCnt]);
                list($gesya_mei, $rosen_mei) = $db->getRow($query);
            }

            $arg["data"]["JOSYA_".$tugakuCnt] = $josya_mei;
            $arg["data"]["ROSEN_".$tugakuCnt] = $rosen_mei;
            $arg["data"]["GESYA_".$tugakuCnt] = $gesya_mei;
        }

        //ボタン作成
        makeBtn($objForm, $arg, $model, $row["STAFFCD"]);

        //hidden
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjg060aSubForm1.html", $arg);
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model, $staffcd)
{
    //終了
    $arg["btn_back"] = knjCreateBtn($objForm, "btn_back", "終了", "onclick=\"return btn_submit('subEnd');\"");
}

//Hidden作成
function makeHidden(&$objForm, $model)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
}

?>
