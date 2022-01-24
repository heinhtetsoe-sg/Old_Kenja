<?php

require_once('for_php7.php');

class knja121bForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knja121bindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //警告メッセージを表示しない場合
        if ((isset($model->schregno) && !isset($model->warning)) || !isset($model->schregno)){
            //HTRAINREMARK_DAT 取得
            $query = knja121bQuery::getTrainRow($model);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field;
        }

        if($model->schregno){
            //行動の記録備考取得
            $RowB = array();
            $result = $db->query(knja121bQuery::getBehavior($model));
            while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                $scd = $row["DIV"] .$row["CODE"];
                $RowB["RECORD"][$scd] = $row["RECORD"];
            }
            $result->free();

            //行動の記録
            $koudou = "";
            for($i=1; $i<11; $i++)
            {
                $ival = "1" . sprintf("%02d", $i);
                $koudou = ($i != "1") ? $koudou.','.$RowB["RECORD"][$ival] : $RowB["RECORD"][$ival];

            }
            //特別活動の記録
            for($i=1; $i<4; $i++)
            {
                $ival = "2" . sprintf("%02d", $i);
                $koudou = $koudou.','.$RowB["RECORD"][$ival];
            }

            //HTRAINREMARK_DATのハッシュ値取得
            $hash = ($model->schregno && $Row) ? $model->makeHash($Row, $koudou) : "";
            //ATTEST_OPINIONS_DATのハッシュ値取得
            $opinion = $db->getRow(knja121bQuery::getOpinionsDat($model), DB_FETCHMODE_ASSOC);

            //ハッシュ値の比較
            if(($opinion && $Row && ($opinion["OPINION"] != $hash)) || (!$hash && $opinion)) {
                $arg["jscript"] = "alert('署名時のデータと不一致です。')";
            }
        }

        //生徒情報
        $arg["SCHREGNO"] = $model->schregno;
        $arg["NAME"]     = $model->name;

        //活動内容
        $extra = "style=\"height:61px;\"";
        $arg["data"]["TOTALSTUDYACT"] = knjCreateTextArea($objForm, "TOTALSTUDYACT", 4, 41, "soft", $extra, $Row["TOTALSTUDYACT"]);

        //観点
        $arg["data"]["VIEWREMARK"] = knjCreateTextArea($objForm, "VIEWREMARK", 4, 21, "soft", $extra, $Row["VIEWREMARK"]);

        //評価
        $arg["data"]["TOTALSTUDYVAL"] = knjCreateTextArea($objForm, "TOTALSTUDYVAL", 4, 41, "soft", $extra, $Row["TOTALSTUDYVAL"]);

        //出欠の記録備考
        $extra = "style=\"height:20px;\"";
        $arg["data"]["ATTENDREC_REMARK"] = knjCreateTextArea($objForm, "ATTENDREC_REMARK", 1, 40, "soft", $extra, $Row["ATTENDREC_REMARK"]);

        //総合所見
        $extra = "style=\"height:385px;\"";
        $arg["data"]["TOTALREMARK"] = knjCreateTextArea($objForm, "TOTALREMARK", 29, 31, "soft", $extra, $Row["TOTALREMARK"]);

        //署名チェック
        $query = knja121bQuery::getOpinionsWk($model);
        $check = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $opinion = ($check["CHAGE_OPI_SEQ"] || $check["LAST_OPI_SEQ"]) ? false : true;

        //ボタン作成
        makeBtn($objForm, $arg, $model, $opinion);

        //hiddenを作成する
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knja121bForm1.html", $arg);
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model, $opinion)
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

    //行動の記録備考
    $extra = " onclick=\"return btn_submit('koudou');\"";
    $arg["button"]["koudou"] = knjCreateBtn($objForm, "koudou", "行動の記録・特別活動の記録", $extra);

    //通知票所見参照
    $extra = " onclick=\"return btn_submit('tuutihyou');\"";
    $arg["button"]["tuutihyou"] = knjCreateBtn($objForm, "tuutihyou", "通知票所見参照", $extra);
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
            $arg["jscript"] = "top.left_frame.nextLink('".$order."')";
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
