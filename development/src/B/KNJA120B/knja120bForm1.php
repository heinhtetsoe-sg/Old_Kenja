<?php
class knja120bForm1 {
    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knja120bindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //警告メッセージを表示しない場合
        if ((isset($model->schregno) && !isset($model->warning)) || !isset($model->schregno)){
            //HTRAINREMARK_DAT 取得
            $query = knja120bQuery::getTrainRow($model);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field;
        }

        if($model->schregno){

            //学習記録データ
            $query = knja120bQuery::getStudyRec($model);
            $result = $db->query($query);
            $study = "";
            while ($studyRow = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $study .= $studyRow["CLASSCD"].$studyRow["SUBCLASSCD"].$studyRow["CLASSNAME"].$studyRow["SUBCLASSNAME"].
                          $studyRow["VALUATION"].$studyRow["GET_CREDIT"].$studyRow["ADD_CREDIT"].$studyRow["COMP_CREDIT"];
            }

            //出欠記録データ
            $attend = $db->getRow(knja120bQuery::getAttendRec($model), DB_FETCHMODE_ASSOC);

            //HTRAINREMARK_DATのハッシュ値取得
            $hash = ($model->schregno && $Row) ? $model->makeHash($Row, $study, $attend) : "";
            //ATTEST_OPINIONS_DATのハッシュ値取得
            $opinion = $db->getRow(knja120bQuery::getOpinionsDat($model), DB_FETCHMODE_ASSOC);

            //ハッシュ値の比較
            if(($opinion && $Row && ($opinion["OPINION"] != $hash)) || (!$hash && $opinion)) {
                $arg["jscript"] = "alert('署名時のデータと不一致です。')";
            }
        }

        //生徒情報
        $arg["SCHREGNO"] = $model->schregno;
        $arg["NAME"]     = $model->name;

        if ($model->Properties["seitoSidoYorokuFieldSize"] == 1) {
            //活動内容
            $extra = "style=\"height:120px;\"";
            $arg["data"]["TOTALSTUDYACT"] = knjCreateTextArea($objForm, "TOTALSTUDYACT", 8, 45, "soft", $extra, $Row["TOTALSTUDYACT"]);
            $arg["data"]["TOTALSTUDYACT_TYUI"] = '(全角22文字X8行まで)';

            //評価
            $extra = "style=\"height:120px;\"";
            $arg["data"]["TOTALSTUDYVAL"] = knjCreateTextArea($objForm, "TOTALSTUDYVAL", 8, 45, "soft", $extra, $Row["TOTALSTUDYVAL"]);
            $arg["data"]["TOTALSTUDYVAL_TYUI"] = '(全角22文字X8行まで)';

            //出欠の記録備考
            $extra = "style=\"height:35px;\"";
            $arg["data"]["ATTENDREC_REMARK"] = knjCreateTextArea($objForm, "ATTENDREC_REMARK", 3, 81, "soft", $extra, $Row["ATTENDREC_REMARK"]);
            $arg["data"]["ATTENDREC_REMARK_TYUI"] = '(全角40文字X2行まで)';
        } else {
            //活動内容
            $extra = "style=\"height:63px;\"";
            $arg["data"]["TOTALSTUDYACT"] = knjCreateTextArea($objForm, "TOTALSTUDYACT", 5, 23, "soft", $extra, $Row["TOTALSTUDYACT"]);
            $arg["data"]["TOTALSTUDYACT_TYUI"] = '(全角11文字X4行まで)';

            //評価
            $extra = "style=\"height:90px;\"";
            $arg["data"]["TOTALSTUDYVAL"] = knjCreateTextArea($objForm, "TOTALSTUDYVAL", 6, 23, "soft", $extra, $Row["TOTALSTUDYVAL"]);
            $arg["data"]["TOTALSTUDYVAL_TYUI"] = '(全角11文字X6行まで)';

            //出欠の記録備考
            $extra = "style=\"height:35px;\"";
            $arg["data"]["ATTENDREC_REMARK"] = knjCreateTextArea($objForm, "ATTENDREC_REMARK", 3, 41, "soft", $extra, $Row["ATTENDREC_REMARK"]);
            $arg["data"]["ATTENDREC_REMARK_TYUI"] = '(全角20文字X2行まで)';
        }

        if ($model->Properties["seitoSidoYoroku_dat_TotalstudyactSize"]) {
            //活動内容
            $height = $model->totalstudyact_gyou * 13.5 + ($model->totalstudyact_gyou -1 ) * 3 + 5;
            $extra = "style=\"height:{$height}px;\" ";
            $arg["data"]["TOTALSTUDYACT"] = KnjCreateTextArea($objForm, "TOTALSTUDYACT", $model->totalstudyact_gyou, ($model->totalstudyact_moji * 2 + 1), "soft", $extra, $Row["TOTALSTUDYACT"]);
            $arg["data"]["TOTALSTUDYACT_TYUI"] = "(全角{$model->totalstudyact_moji}文字{$model->totalstudyact_gyou}行まで)";
        }
        if ($model->Properties["seitoSidoYoroku_dat_TotalstudyvalSize"]) {
            //評価
            $height = $model->totalstudyval_gyou * 13.5 + ($model->totalstudyval_gyou -1 ) * 3 + 5;
            $extra = "style=\"height:{$height}px;\" ";
            $arg["data"]["TOTALSTUDYVAL"] = KnjCreateTextArea($objForm, "TOTALSTUDYVAL", $model->totalstudyval_gyou, ($model->totalstudyval_moji * 2 + 1), "soft", $extra, $Row["TOTALSTUDYVAL"]);
            $arg["data"]["TOTALSTUDYVAL_TYUI"] = "(全角{$model->totalstudyval_moji}文字{$model->totalstudyval_gyou}行まで)";
        }

        if ($model->Properties["seitoSidoYorokuSpecialactremarkFieldSize"] == 1) {
            //特別活動所見
            $extra = "style=\"height:145px;\"";
            $arg["data"]["SPECIALACTREMARK"] = knjCreateTextArea($objForm, "SPECIALACTREMARK", 10, 45, "soft", $extra, $Row["SPECIALACTREMARK"]);
            $arg["data"]["SPECIALACTREMARK_TYUI"] = '(全角22文字X10行まで)';
        } else {
            //特別活動所見
            $extra = "style=\"height:90px;\"";
            $arg["data"]["SPECIALACTREMARK"] = knjCreateTextArea($objForm, "SPECIALACTREMARK", 6, 23, "soft", $extra, $Row["SPECIALACTREMARK"]);
            $arg["data"]["SPECIALACTREMARK_TYUI"] = '(全角11文字X6行まで)';
        }

        if ($model->Properties["seitoSidoYorokuSougouFieldSize"] == 1) {
            //総合所見
            $extra = "style=\"height:120px;\"";
            $arg["data"]["TOTALREMARK"] = knjCreateTextArea($objForm, "TOTALREMARK", 8, 133, "soft", $extra, $Row["TOTALREMARK"]);
            $arg["data"]["TOTALREMARK_TYUI"] = '(全角66文字X8行まで)';
        } elseif ($model->Properties["seitoSidoYorokuFieldSize"] == 1) {
            //総合所見
            $extra = "style=\"height:105px;\"";
            $arg["data"]["TOTALREMARK"] = knjCreateTextArea($objForm, "TOTALREMARK", 7, 133, "soft", $extra, $Row["TOTALREMARK"]);
            $arg["data"]["TOTALREMARK_TYUI"] = '(全角66文字X7行まで)';
        } else {
            //総合所見
            $extra = "style=\"height:90px;\"";
            $arg["data"]["TOTALREMARK"] = knjCreateTextArea($objForm, "TOTALREMARK", 6, 89, "soft", $extra, $Row["TOTALREMARK"]);
            $arg["data"]["TOTALREMARK_TYUI"] = '(全角44文字X6行まで)';
        }

        //署名チェック
        $query = knja120bQuery::getOpinionsWk($model);
        $check = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $opinion = ($check["CHAGE_OPI_SEQ"] || $check["LAST_OPI_SEQ"]) ? false : true;

        //ボタン作成
        makeBtn($objForm, $arg, $db, $model, $opinion);

        //hiddenを作成する
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();
        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knja120bForm1.html", $arg);
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg, &$db, $model, $opinion)
{
    if((AUTHORITY < DEF_UPDATE_RESTRICT) || !$opinion){
        //更新ボタン
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", "disabled");
        //前の生徒へボタン
        $extra = "style=\"width:130px\" onclick=\"top.left_frame.nextStudentOnly('pre');\"";
        $arg["button"]["btn_up_pre"]   = knjCreateBtn($objForm, "btn_up_pre", "前の生徒へ", $extra);
        //次の生徒へボタン
        $extra = "style=\"width:130px\" onclick=\"top.left_frame.nextStudentOnly('next');\"";
        $arg["button"]["btn_up_next"]  = knjCreateBtn($objForm, "btn_up_next", "次の生徒へ", $extra);
    } else {
        //更新ボタン
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", "onclick=\"return btn_submit('update');\"");
        //更新後前の生徒へボタン
        $arg["button"]["btn_up_next"] = updateNext($model, $objForm, $arg, 'btn_update');
    }
    //取消ボタン
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", "onclick=\"return btn_submit('clear');\"");
    //終了ボタン
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");

    //部活動参照ボタンを作成する
    $extra = " onclick=\" return btn_submit('bukatu');\"";
    $arg["button"]["bukatu"] = KnjCreateBtn($objForm, "bukatu", "部活動参照", $extra);
    //委員会参照ボタンを作成する
    $extra = " onclick=\" return btn_submit('iinkai');\"";
    $arg["button"]["iinkai"] = KnjCreateBtn($objForm, "iinkai", "委員会参照", $extra);
    //通知票所見参照
    $cnt = $db->getOne(knja120bQuery::getKindaiJudgment($model));
    if (!$cnt) {
        $extra = " onclick=\"return btn_submit('tuutihyou');\"";
        $arg["button"]["tuutihyou"] = knjCreateBtn($objForm, "tuutihyou", "通知票所見参照", $extra);
    }
    //資格参照
    $extra = " onclick=\"return btn_submit('sikaku');\"";
    $arg["button"]["sikaku"] = knjCreateBtn($objForm, "sikaku", "資格参照", $extra);
    //出欠備考参照ボタン
    $sdate = CTRL_YEAR.'-04-01';
    $edate = (CTRL_YEAR+1).'-03-31';
    $extra  = " onclick=\"loadwindow('../../X/KNJXATTEND_REMARK/index.php?";
    $extra .= "YEAR={$model->exp_year}&";
    $extra .= "SCHREGNO={$model->schregno}&";
    $extra .= "SDATE={$sdate}&";
    $extra .= "EDATE={$edate}',0,0,420,300);return;\"";
    $arg["button"]["syukketu"] = KnjCreateBtn($objForm, "syukketu", "出欠備考参照", $extra);
    //調査書(進学用)出欠の記録参照
    $extra = " onclick=\"return btn_submit('tyousasyo');\"";
    $arg["button"]["tyousasyo"] = knjCreateBtn($objForm, "tyousasyo", "調査書(進学用)出欠の記録参照", $extra);

    //CSV処理用フィールドサイズ取得
    if ($model->Properties["seitoSidoYorokuFieldSize"] == 1) {
        $fieldActSize = "TOTALSTUDYACT=528,";
        $fieldValSize = "TOTALSTUDYVAL=528,";
    } else {   
        $fieldActSize = "TOTALSTUDYACT=132,";
        $fieldValSize = "TOTALSTUDYVAL=198,";
    }
    if ($model->Properties["seitoSidoYoroku_dat_TotalstudyactSize"]) {
        $fieldActSize = "TOTALSTUDYACT=".($model->totalstudyact_moji * 3 * $model->totalstudyact_gyou) .",";
    }
    if ($model->Properties["seitoSidoYoroku_dat_TotalstudyvalSize"]) {
        $fieldValSize = "TOTALSTUDYVAL=".($model->totalstudyval_moji * 3 * $model->totalstudyval_gyou) .",";
    }
    $fieldSize = $fieldActSize.$fieldValSize;
    if ($model->Properties["seitoSidoYorokuSpecialactremarkFieldSize"] == 1) {
        $fieldSize .= "SPECIALACTREMARK=660,";
    } else {
        $fieldSize .= "SPECIALACTREMARK=198,";
    }
    if ($model->Properties["seitoSidoYorokuSougouFieldSize"] == 1) {
        $fieldSize .= "TOTALREMARK=1584,";
    } else if ($model->Properties["seitoSidoYorokuFieldSize"] == 1) {
        $fieldSize .= "TOTALREMARK=1386,";
    } else {
        $fieldSize .= "TOTALREMARK=792,";
    }
    if ($model->Properties["seitoSidoYorokuFieldSize"] == 1) {
        $fieldSize .= "ATTENDREC_REMARK=240,";
    } else {
        $fieldSize .= "ATTENDREC_REMARK=120,";
    }
    $fieldSize .= "VIEWREMARK=0,";
    $fieldSize .= "BEHAVEREC_REMARK=0";

    //CSVボタン
    $extra = ($model->schregno) ? " onClick=\" wopen('".REQUESTROOT."/X/KNJX180B/knjx180bindex.php?cmd=sign&FIELDSIZE=".$fieldSize."&EXP_YEAR=".$model->exp_year."&EXP_SEMESTER=".$model->exp_semester."&SCHREGNO=".$model->schregno."&AUTH=".AUTHORITY."','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"" : "disabled";
    $arg["button"]["btn_csv"] = KnjCreateBtn($objForm, "btn_csv", "ＣＳＶ処理", $extra);

}

function updateNext(&$model, &$objForm, &$arg, $btn='btn_update'){
    //更新ボタン
    $objForm->ae( array("type"      =>  "button",
                        "name"      =>  "btn_up_pre",
                        "value"     =>  "更新後前の生徒へ",
                        "extrahtml" =>  "style=\"width:130px\" onclick=\"top.left_frame.updateNext(self, 'pre','".$btn ."');\""));

    //更新ボタン
    $objForm->ae( array("type"      =>  "button",
                        "name"      =>  "btn_up_next",
                        "value"     =>  "更新後次の生徒へ",
                        "extrahtml" =>  "style=\"width:130px\" onclick=\"top.left_frame.updateNext(self, 'next','".$btn ."');\""));

    if ($_POST["_ORDER"] == "pre" || $_POST["_ORDER"] == "next" ){
       $order = $_POST["_ORDER"];
       if (!isset($model->warning)){
            $arg["jscript"] = "updBtnNotDisp(); top.left_frame.nextLink('".$order."')";
            unset($model->message);
       }
    }
    $objForm->ae( array("type"      => "hidden",
                        "name"      => "_ORDER" ));
                    
    return $objForm->ge("btn_up_pre") .$objForm->ge("btn_up_next");
}

//Hidden作成
function makeHidden(&$objForm, $model)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
}
?>
