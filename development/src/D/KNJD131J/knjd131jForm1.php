<?php

require_once('for_php7.php');

class knjd131jForm1
{
    public function main(&$model)
    {
        $objForm = new form();
        $arg["start"] = $objForm->get_start("edit", "POST", "knjd131jindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();
        
        //学年チェック
        if ($model->schregno != "" && $model->cmd == "edit") {
            $model->disable = "";
            $getCheckGrade = $db->getOne(knjd131jQuery::checkGrade($model));
            if ($getCheckGrade == 0) {
                $arg["jscript"] = "OnGradeError();";
                $model->disable = "disabled";
            }
        }

        //警告メッセージを表示しない場合
        if ((isset($model->schregno) && !isset($model->warning)) || !isset($model->schregno)) {
            $row = $db->getRow(knjd131jQuery::getTrainRow($model, $model->schregno), DB_FETCHMODE_ASSOC);
            $arg["NOT_WARNING"] = 1;
        } else {
            $row =& $model->field;
        }

        //生徒情報
        $arg["data"]["SCHREGNO"] = $model->schregno;
        $arg["data"]["NAME"]     = $model->name;

        //extra
        $extra_12 = "style=\"height:100px;\"";
        $extra_3  = "style=\"height:275px;\"";
                
        //総合的な学習の時間
        $arg["data"]["DIV1_REMARK"] = knjCreateTextArea($objForm, "DIV1_REMARK", 7, 61, "soft", $extra_12, $row["DIV1_REMARK"]);
                
        //道徳
        $arg["data"]["DIV2_REMARK"] = knjCreateTextArea($objForm, "DIV2_REMARK", 7, 61, "soft", $extra_12, $row["DIV2_REMARK"]);

        //活動と奉仕の記録
        $arg["data"]["DIV3_REMARK"] = knjCreateTextArea($objForm, "DIV3_REMARK", 20, 61, "soft", $extra_3, $row["DIV3_REMARK"]);

        //ボタン作成
        makeBtn($objForm, $arg, $model, $db);

        //hidden
        makeHidden($objForm, $model);

        if (get_count($model->warning) == 0 && $model->cmd != "clear") {
            $arg["next"] = "NextStudent2(0);";
        } elseif ($model->cmd == "clear") {
            $arg["next"] = "NextStudent2(1);";
        }

        //画面のリロード
        if ($model->cmd == "updEdit") {
            $arg["reload"] = "parent.left_frame.btn_submit('list');";
        }

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();

        View::toHTML($model, "knjd131jForm1.html", $arg);
    }
}

//Hidden作成
function makeHidden(&$objForm, $model)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
    knjCreateHidden($objForm, "SCHREGNOS");
    knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model, $db)
{
    //更新
    $disable = (AUTHORITY == DEF_UPDATABLE || AUTHORITY == DEF_UPDATE_RESTRICT) ? "" : "disabled";
    if ($model->disable != "" && $disable == "") {
        $disable = "disabled";
    }
    $extra = $disable." onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //更新後前後の生徒へ
    if (AUTHORITY == DEF_UPDATABLE || AUTHORITY == DEF_UPDATE_RESTRICT) {
        if ($model->disable != "") {
            $extra = "disabled style=\"width:130px\"";
            $arg["button"]["btn_up_pre"] = knjCreateBtn($objForm, "btn_up_pre", "更新後前の生徒へ", $extra);
            $arg["button"]["btn_up_next"] = knjCreateBtn($objForm, "btn_up_next", "更新後次の生徒へ", $extra);
        } else {
            $arg["button"]["btn_up_next"] = View::updateNext2($model, $objForm, $model->schregno, "SCHREGNO", "edit", "update");
        }
    } else {
        $extra = "disabled style=\"width:130px\"";
        $arg["button"]["btn_up_pre"] = knjCreateBtn($objForm, "btn_up_pre", "更新後前の生徒へ", $extra);
        $arg["button"]["btn_up_next"] = knjCreateBtn($objForm, "btn_up_next", "更新後次の生徒へ", $extra);
    }
    //取消
    $extra = "onclick=\"return btn_submit('clear');\"";
    $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}
