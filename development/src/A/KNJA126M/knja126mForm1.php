<?php

require_once('for_php7.php');

class knja126mForm1
{
    function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knja126mindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //警告メッセージを表示しない場合
        if ((isset($model->schregno) && !isset($model->warning)) || !isset($model->schregno)){
            $row = $db->getRow(knja126mQuery::getTrainRow($model->schregno, $model->exp_year), DB_FETCHMODE_ASSOC);
            $arg["NOT_WARNING"] = 1;
        } else {
            $row =& $model->field;
        }

        //署名チェック
        if($model->schregno){
            //学習記録データ
            $query = knja126mQuery::getStudyRec($model);
            $result = $db->query($query);
            $study = "";
            while ($studyRow = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $study .= $studyRow["CLASSCD"].$studyRow["SCHOOL_KIND"].$studyRow["CURRICULUM_CD"].$studyRow["SUBCLASSCD"].$studyRow["CLASSNAME"].$studyRow["SUBCLASSNAME"].
                              $studyRow["VALUATION"].$studyRow["GET_CREDIT"].$studyRow["ADD_CREDIT"].$studyRow["COMP_CREDIT"];
                } else {
                    $study .= $studyRow["CLASSCD"].$studyRow["SUBCLASSCD"].$studyRow["CLASSNAME"].$studyRow["SUBCLASSNAME"].
                              $studyRow["VALUATION"].$studyRow["GET_CREDIT"].$studyRow["ADD_CREDIT"].$studyRow["COMP_CREDIT"];
                }
            }

            //ホームルーム出席履歴データ
            $attend = $db->getOne(knja126mQuery::getHrAttend($model));

            //HTRAINREMARK_DATのハッシュ値取得
            $hash = ($model->schregno && $row) ? $model->makeHash($row, $study, $attend) : "";
            //ATTEST_OPINIONS_DATのハッシュ値取得
            $opinion = $db->getRow(knja126mQuery::getOpinionsDat($model), DB_FETCHMODE_ASSOC);

            //ハッシュ値の比較
            if(($opinion && $row && ($opinion["OPINION"] != $hash)) || (!$hash && $opinion)) {
                $arg["jscript"] = "alert('署名時のデータと不一致です。')";
            }
        }

        //生徒情報
        $arg["SCHREGNO"] = $model->schregno;
        $arg["NAME"]     = $model->name;

        //活動内容
        $arg["data"]["TOTALSTUDYACT"] = KnjCreateTextArea($objForm, "TOTALSTUDYACT", 3, 89, "soft", $extra, $row["TOTALSTUDYACT"]);
        
        //評価
        $arg["data"]["TOTALSTUDYVAL"] = KnjCreateTextArea($objForm, "TOTALSTUDYVAL", 3, 89, "soft", $extra, $row["TOTALSTUDYVAL"]);

        //特別活動所見
        $extra = "style=\"height:90px;\"";
        $arg["data"]["SPECIALACTREMARK"] = knjCreateTextArea($objForm, "SPECIALACTREMARK", 6, 23, "soft", $extra, $row["SPECIALACTREMARK"]);

        //総合所見
        $extra = "style=\"height:90px;\"";
        $arg["data"]["TOTALREMARK"] = knjCreateTextArea($objForm, "TOTALREMARK", 6, 45, "soft", $extra, $row["TOTALREMARK"]);

        //評価
        $arg["data"]["ATTENDREC_REMARK"] = knjCreateTextBox($objForm, $row["ATTENDREC_REMARK"], "ATTENDREC_REMARK", 80, 80, "");

        //署名チェック
        $query = knja126mQuery::getOpinionsWk($model);
        $check = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $opinion = ($check["CHAGE_OPI_SEQ"] || $check["LAST_OPI_SEQ"]) ? false : true;

        //学校種別
        $schoolkind = $db->getOne(knja126mQuery::getSchoolKind($model));

        //ボタン作成
        makeBtn($objForm, $arg, $model, $opinion, $schoolkind);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "SCHREGNO", $model->schregno);

        //インラインフレーム
        $arg["IFRAME"] = VIEW::setIframeJs();

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knja126mForm1.html", $arg);
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model, $opinion, $schoolkind)
{
    if((AUTHORITY < DEF_UPDATE_RESTRICT) || !$opinion || $schoolkind != 'H'){
        //更新ボタン
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", "disabled");
        //前の生徒へボタン
        $extra = "style=\"width:130px\" onclick=\"top.left_frame.nextStudentOnly('pre');\"";
        $arg["button"]["btn_up_pre"] = knjCreateBtn($objForm, "btn_up_pre", "前の生徒へ", $extra);
        //次の生徒へボタン
        $extra = "style=\"width:130px\" onclick=\"top.left_frame.nextStudentOnly('next');\"";
        $arg["button"]["btn_up_next"] = knjCreateBtn($objForm, "btn_up_next", "次の生徒へ", $extra);
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

    //部活動参照ボタン
    $extra = "onclick=\"return btn_submit('subform2');\"";
    $arg["button"]["btn_club"] = KnjCreateBtn($objForm, "btn_club", "部活動参照", $extra);
    //委員会参照ボタン
    $extra = "onclick=\"return btn_submit('subform3');\"";
    $arg["button"]["btn_committee"] = KnjCreateBtn($objForm, "btn_committee", "委員会参照", $extra);
    //資格参照ボタン
    $extra = "onclick=\"return btn_submit('subform1');\"";
    $arg["button"]["SIKAKU_SANSYO"] = KnjCreateBtn($objForm, "SIKAKU_SANSYO", "資格参照", $extra);
    //調査書の出欠備考参照ボタン
    $extra = "onclick=\"return btn_submit('subform4');\"";
    $arg["button"]["TYOSASYO_SANSYO"] = KnjCreateBtn($objForm, "TYOSASYO_SANSYO", "調査書(進学用)の出欠の記録参照", $extra);

    //CSV処理
    $fieldSize  = "TOTALSTUDYACT=132,";
    $fieldSize .= "TOTALSTUDYVAL=132,";
    $fieldSize .= "SPECIALACTREMARK=198,";
    $fieldSize .= "TOTALREMARK=396,";
    $fieldSize .= "ATTENDREC_REMARK=120";

    //ＣＳＶ出力ボタン
    $extra = ($model->schregno && $schoolkind == 'H') ? "onClick=\" wopen('".REQUESTROOT."/X/KNJX_A125M/knjx_a125mindex.php?cmd=sign&FIELDSIZE=".$fieldSize."&EXP_YEAR=".$model->exp_year."&EXP_SEMESTER=".$model->exp_semester."&SCHREGNO=".$model->schregno."&AUTH=".AUTHORITY."&PRGID=".PROGRAMID."','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"" : "disabled";
    $arg["button"]["btn_csv"] = KnjCreateBtn($objForm, "btn_csv", "ＣＳＶ出力", $extra);
}

function updateNext(&$model, &$objForm, &$arg, $btn='btn_update'){

    //更新ボタン
    $extra = "style=\"width:130px\" onclick=\"top.left_frame.updateNext(self, 'pre','".$btn ."');\"";
    $btn_up_pre = KnjCreateBtn($objForm, "btn_up_pre", "更新後前の生徒へ", $extra);

    //更新ボタン
    $extra = "style=\"width:130px\" onclick=\"top.left_frame.updateNext(self, 'next','".$btn ."');\"";
    $btn_up_next = KnjCreateBtn($objForm, "btn_up_next", "更新後次の生徒へ", $extra);

    if ($_POST["_ORDER"] == "pre" || $_POST["_ORDER"] == "next" ){
       $order = $_POST["_ORDER"];
       if (!isset($model->warning)){
            $arg["jscript"] = "top.left_frame.nextLink('".$order."')";
            unset($model->message);
       }
    }

    knjCreateHidden($objForm, "_ORDER");
                    
    return $btn_up_pre .$btn_up_next;
}
?>
