<?php

require_once("for_php7.php");

class shokenlist
{
    public function main(&$model)
    {
        $objForm = new form();
        $arg["start"] = $objForm->get_start("detail", "POST", "knje012bindex.php", "", "detail");

        //DB OPEN
        $db = Query::dbCheckOut();

        //生徒情報
        $arg["NAME_SHOW"] = $model->schregno."  :  ".$model->name;

        //項目の表示内容を設定
        if ($model->cmd === 'shokenlist1') {
            $arg["shokenlist1"] = "1";
        } elseif ($model->cmd === 'shokenlist2') {
            $arg["shokenlist2"] = "1";
            //総合所見（３分割）
            if ($model->Properties["train_ref_1_2_3_use_J"] == '1') {
                $arg["show_train_ref_1_2_3_use_J"] = "1";
            } else {
                $arg["show_totalremark"] = "1";
            }
        } elseif ($model->cmd === 'shokenlist3') {
            $arg["shokenlist3"] = "1";
        } elseif ($model->cmd === 'shokenlist4') {
            $arg["shokenlist4"] = "1";
        }

        //HTRAINREMARK_DAT 取得
        $model->data["YEAR"] = "";
        $result = $db->query(knje012bQuery::getTrainRow($model, "sanshou"));
        while ($Row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //学習活動
            $extra = "style=\"height:118px;\"";
            $Row["TOTALSTUDYACT"] = knjCreateTextArea($objForm, "TOTALSTUDYACT", 8, 11, "soft", $extra, $Row["TOTALSTUDYACT"]);

            //観点
            $extra = "style=\"height:118px;\"";
            $Row["VIEWREMARK"] = knjCreateTextArea($objForm, "VIEWREMARK", 8, 21, "soft", $extra, $Row["VIEWREMARK"]);

            //評価
            $extra = "style=\"height:118px;\"";
            $Row["TOTALSTUDYVAL"] = knjCreateTextArea($objForm, "TOTALSTUDYVAL", 8, 31, "soft", $extra, $Row["TOTALSTUDYVAL"]);

            //特別活動所見
            if ($model->Properties["seitoSidoYorokuSpecialactremarkFieldSize"] == 1) {
                $extra = "style=\"height:145px;\"";
                $Row["SPECIALACTREMARK"] = knjCreateTextArea($objForm, "SPECIALACTREMARK", 10, 45, "soft", $extra, $Row["SPECIALACTREMARK"]);
            } else {
                $extra = "style=\"height:90px;\"";
                $Row["SPECIALACTREMARK"] = knjCreateTextArea($objForm, "SPECIALACTREMARK", 6, 23, "soft", $extra, $Row["SPECIALACTREMARK"]);
            }

            //総合所見（３分割）
            //1)学習における特徴等　2)行動の特徴、特技等
            $Row["TRAIN_REF1"] = getTextOrArea($objForm, "TRAIN_REF1", $model->train_ref1_moji, $model->train_ref1_gyou, $Row["TRAIN_REF1"], $model);
            //3)部活動、ボランティア活動等　4)取得資格、検定等
            $Row["TRAIN_REF2"] = getTextOrArea($objForm, "TRAIN_REF2", $model->train_ref2_moji, $model->train_ref2_gyou, $Row["TRAIN_REF2"], $model);
            //5)その他
            $Row["TRAIN_REF3"] = getTextOrArea($objForm, "TRAIN_REF3", $model->train_ref3_moji, $model->train_ref3_gyou, $Row["TRAIN_REF3"], $model);
            //総合所見
            if ($model->Properties["seitoSidoYorokuSougouFieldSize"] == 1) {
                $extra = "style=\"height:120px;\"";
                $Row["TOTALREMARK"] = knjCreateTextArea($objForm, "TOTALREMARK", 8, 133, "soft", $extra, $Row["TOTALREMARK"]);
            } elseif ($model->Properties["seitoSidoYorokuFieldSize"] == 1) {
                $extra = "style=\"height:105px;\"";
                $Row["TOTALREMARK"] = knjCreateTextArea($objForm, "TOTALREMARK", 7, 133, "soft", $extra, $Row["TOTALREMARK"]);
            } else {
                $extra = "style=\"height:90px;\"";
                $Row["TOTALREMARK"] = knjCreateTextArea($objForm, "TOTALREMARK", 6, 89, "soft", $extra, $Row["TOTALREMARK"]);
            }

            //出欠の記録備考
            if ($model->Properties["seitoSidoYorokuFieldSize"] == 1) {
                $extra = "style=\"height:35px;\"";
                $Row["ATTENDREC_REMARK"] = knjCreateTextArea($objForm, "ATTENDREC_REMARK", 3, 81, "soft", $extra, $Row["ATTENDREC_REMARK"]);
            } else {
                $extra = "style=\"height:35px;\"";
                $Row["ATTENDREC_REMARK"] = knjCreateTextArea($objForm, "ATTENDREC_REMARK", 3, 41, "soft", $extra, $Row["ATTENDREC_REMARK"]);
            }

            //生徒会・学校行事・HR活動
            $extra = "";
            $Row["SPECIALACTREC"] = knjCreateTextArea($objForm, "SPECIALACTREC", 6, 37, "soft", $extra, $Row["SPECIALACTREC"]);

            //クラブ活動
            $extra = "";
            $Row["CLUBACT"] = knjCreateTextArea($objForm, "CLUBACT", 6, 37, "soft", $extra, $Row["CLUBACT"]);

            $arg["data"][] = $Row;
        }
        $result->free();

        //戻るボタンを作成する
        $extra = "onclick=\"return parent.closeit()\"";
        $arg["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJA120B");
        knjCreateHidden($objForm, "SCHREGNO", "$model->schregno");

        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを$arg経由で渡す
        View::toHTML($model, "shokenlist.html", $arg);
    }
}
/********************************************** 以下関数 ******************************************************/
//テキストボックスorテキストエリア作成
function getTextOrArea(&$objForm, $name, $moji, $gyou, $val, $model)
{
    $retArg = "";
    if ($gyou > 1) {
        //textArea
        $minusHasu = 0;
        $minus = 0;
        if ($gyou >= 5) {
            $minusHasu = $gyou % 5;
            $minus = ($gyou / 5) > 1 ? ($gyou / 5) * 6 : 5;
        }
        $height = $gyou * 13.5 + ($gyou -1) * 3 + (5 - ($minus + $minusHasu));
        $extra = "style=\"height:".$height."px;\" onkeyup=\"charCount(this.value, $gyou, ($moji * 2), true);\"";
        $retArg = knjCreateTextArea($objForm, $name, $gyou, ($moji * 2) + 1, "soft", $extra, $val);
    } else {
        //textbox
        $extra = "onkeypress=\"btn_keypress();\"";
        $retArg = knjCreateTextBox($objForm, $val, $name, ($moji * 2), $moji, $extra);
    }
    return $retArg;
}
