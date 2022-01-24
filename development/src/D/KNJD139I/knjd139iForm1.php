<?php

require_once('for_php7.php');

class knjd139iForm1
{
    public function main(&$model)
    {
        // 表示/非表示及びテキストサイズ設定テーブル。
        // 該当学年で非表示にする場合は、dispflgsettingの該当項目を0にすること。
        // 該当学年で表示する場合は、$setdispinfoの対応項目設定、及び該当箇所の処理との整合性を取ること。
        // 「2学期以降表示」等、細かい制御については、「"recact"」の処理を参照。

        $dispsetting = array();
        $setdispinfo = array();
        //1年
        $dispflgsetting[] = array("recact"=>1,
                                  "totalstdy"=>"0",
                                  "moral"=>"1",
                                  "act_club"=>"0",
                                  "otherttl"=>"1",
                                  "other"=>"1");
        $setdispinfo[]    = array("recact"=>array(),     //非表示なので設定不要
                                  "totalstdy"=>array(),  //非表示なので設定不要
                                  "moral"=>array("title"=>array("row"=>1, "col"=>25), "data"=>array("row"=>4, "col"=>60)),
                                  "act_club"=>array(),   //非表示なので設定不要
                                  "other"=>array("attend"=>array("row"=>3, "col"=>32), "title"=>array("row"=>10, "col"=>50), "remark"=>array("row"=>10, "col"=>50)));
        //2年
        $dispflgsetting[] = array("recact"=>1,
                                  "totalstdy"=>"0",
                                  "moral"=>"1",
                                  "act_club"=>"0",
                                  "otherttl"=>"1",
                                  "other"=>"1");
        $setdispinfo[]    = array("recact"=>array(),     //非表示なので設定不要
                                  "totalstdy"=>array(),  //非表示なので設定不要
                                  "moral"=>array("title"=>array("row"=>1, "col"=>25), "data"=>array("row"=>4, "col"=>60)),
                                  "act_club"=>array(),   //非表示なので設定不要
                                  "other"=>array("attend"=>array("row"=>3, "col"=>32), "title"=>array("row"=>10, "col"=>50), "remark"=>array("row"=>10, "col"=>50)));
        //3年
        $dispflgsetting[] = array("recact"=>1,
                                  "totalstdy"=>"1",
                                  "moral"=>"1",
                                  "act_club"=>"0",
                                  "otherttl"=>"1",
                                  "other"=>"1");
        $setdispinfo[]    = array("recact"=>array(),     //非表示なので設定不要
                                  "totalstdy"=>array("title"=>array("row"=>1, "col"=>25), "data"=>array("row"=>4, "col"=>60)),
                                  "moral"=>array("title"=>array("row"=>1, "col"=>25), "data"=>array("row"=>4, "col"=>60)),
                                  "act_club"=>array(),   //非表示なので設定不要
                                  "other"=>array("attend"=>array("row"=>3, "col"=>32), "title"=>array("row"=>10, "col"=>50), "remark"=>array("row"=>10, "col"=>50)));
        //4年
        $dispflgsetting[] = array("recact"=>1,
                                  "totalstdy"=>"1",
                                  "moral"=>"1",
                                  "act_club"=>"0",
                                  "otherttl"=>"1",
                                  "other"=>"1");
        $setdispinfo[]    = array("recact"=>array(),     //非表示なので設定不要
                                  "totalstdy"=>array("title"=>array("row"=>1, "col"=>25), "data"=>array("row"=>4, "col"=>60)),
                                  "moral"=>array("title"=>array("row"=>1, "col"=>25), "data"=>array("row"=>4, "col"=>60)),
                                  "act_club"=>array(),   //非表示なので設定不要
                                  "other"=>array("attend"=>array("row"=>3, "col"=>32), "title"=>array("row"=>10, "col"=>50), "remark"=>array("row"=>10, "col"=>50)));
        //5年
        $dispflgsetting[] = array("recact"=>1,
                                  "totalstdy"=>"1",
                                  "moral"=>"1",
                                  "act_club"=>"1",
                                  "otherttl"=>"1",
                                  "other"=>"1");
        $setdispinfo[]    = array("recact"=>array(),     //非表示なので設定不要
                                  "totalstdy"=>array("title"=>array("row"=>1, "col"=>25), "data"=>array("row"=>4, "col"=>60)),
                                  "moral"=>array("title"=>array("row"=>1, "col"=>25), "data"=>array("row"=>4, "col"=>60)),
                                  "act_club"=>array("clubfst"=>array("row"=>2, "col"=>22), "clubsnd"=>array("row"=>2, "col"=>22), "favorite"=>array("row"=>2, "col"=>26)),
                                  "other"=>array("attend"=>array("row"=>3, "col"=>32), "title"=>array("row"=>10, "col"=>50), "remark"=>array("row"=>10, "col"=>50)));
        //6年
        $dispflgsetting[] = array("recact"=>1,
                                  "totalstdy"=>"1",
                                  "moral"=>"1",
                                  "act_club"=>"1",
                                  "otherttl"=>"1",
                                  "other"=>"1");
        $setdispinfo[]    = array("recact"=>array(),     //非表示なので設定不要
                                  "totalstdy"=>array("title"=>array("row"=>1, "col"=>25), "data"=>array("row"=>4, "col"=>60)),
                                  "moral"=>array("title"=>array("row"=>1, "col"=>25), "data"=>array("row"=>4, "col"=>60)),
                                  "act_club"=>array("clubfst"=>array("row"=>2, "col"=>22), "clubsnd"=>array("row"=>2, "col"=>22), "favorite"=>array("row"=>2, "col"=>26)),
                                  "other"=>array("attend"=>array("row"=>3, "col"=>32), "title"=>array("row"=>10, "col"=>50), "remark"=>array("row"=>10, "col"=>50)));

        $fieldsize = "";
        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjd139iindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //生徒情報
        $arg["NAME_SHOW"] = $model->schregno."　".$model->name;

        $arg["dispinputflg"] = "";
        if ($model->name != "") {
            $arg["dispinputflg"] = "1";
        } else {
            $arg["nonedispflg"] = "1";
        }

        //年次取得
        $gradeCd = $db->getOne(knjd139iQuery::getGradeCd($model));

        //学期コンボ
        $query = knjd139iQuery::getSemester($model);
        $model->field["SEMESTER"] = $model->field["SEMESTER"] ? $model->field["SEMESTER"] : $model->exp_semester;
        $extra = "onChange=\"return btn_submit('edit')\"";
        $this->makeCmb($objForm, $arg, $db, $query, $model->field["SEMESTER"], "SEMESTER", $extra, 1, "");

        //観点マスタ
        $bsm_usechkgrade = array("01","02","03","04", "05", "06");
        $maxlen = 0;
        $model->itemArray = array();
        $query = knjd139iQuery::getBehaviorSemesMst($model, $model->grade);
        $result = $db->query($query);
        $tmpval = 0;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $ival = sprintf("%02d", $tmpval);
            if (in_array($gradeCd, $bsm_usechkgrade)) {
                $bsmusedetail = get_count($model->warning) != 0 ? $this->record["RECORD"][$ival] : $row["LABEL"];
            } else {
                $bsmusedetail = get_count($model->warning) != 0 ? $this->record["RECORD"][$ival] : $row["DETAIL"];
            }
            $model->itemArray[$row["VALUE"]] = $bsmusedetail;
            //MAX文字数
            if ($maxlen < mb_strwidth($bsmusedetail)) {
                $maxlen = mb_strwidth($bsmusedetail);
            }
            $tmpval++;
        }
        $result->free();

        //サイズ
        $width = ($maxlen * 8 < 250) ? 250 : $maxlen * 8;
        $arg["RECORD_LABEL_WIDTH"] = $width;
        $arg["RECORD_VALUE_WIDTH"] = 50;
        $arg["MAIN_WIDTH"] = $width + 50;

        //記録の取得
        $Row = $row = array();
        $result = $db->query(knjd139iQuery::getBehavior($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $Row["RECORD"][$row["CODE"]] = $row["RECORD"];
        }
        $result->free();

        $arg["disprecactvflg"] = "";
        $dispchksemester = $dispflgsetting[intval($gradeCd)-1]["recact"];
        $recactvidlist = array();
        $recactvnamelist = array();
        if ($dispchksemester > 0 && $dispchksemester <= $model->field["SEMESTER"]) {
            //学期
            $query = knjd139iQuery::getNameMst($model->exp_year, "D036");
            $result = $db->query($query);
            $sep = "";
            $settxt = "(";
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $settxt .= $sep . $row["LABEL"];
                $recactvidlist[] = $row["VALUE"];
                $sep = " ";
            }
            $settxt .= ")";
            $arg["TEXT_TITLE"] = $settxt;
            $result->free();

            $arg["disprecactvflg"] = "1";
            $csvfieldidx = 1;
            if (is_array($model->itemArray)) {
                foreach ($model->itemArray as $key => $val) {
                    $setData = array();
                    //データ
                    if ($model->Properties["knjdBehaviorsd_UseText"] == "1") {
                        //項目名
                        $setData["RECORD_LABEL"] = $val;
                        $fieldsize .= "RECACT".$csvfieldidx."=1=1=".$val.$settxt.",";
                        //テキスト
                        $extra = "id=\"RECORD".$key."\" STYLE=\"text-align: center\"; onblur=\"chkrecactv(this);\"  onKeyDown=\"keyChangeEntToTab(this);\""; // oncontextmenu=\"kirikae2(this, '".$key."')\";";
                        $setData["RECORD_VALUE"] = knjCreateTextBox($objForm, $Row["RECORD"][$key], "RECORD".$key, 3, 1, $extra);
//                        knjCreateHidden($objForm, "RECORD".$key."_KETA", $displen);
//                        knjCreateHidden($objForm, "RECORD".$key."_GYO", $disprow);
//                        KnjCreateHidden($objForm, "RECORD".$key."_STAT", "statusareaXX".$key);
                        $recactvnamelist[] = "RECORD".$key;
                    } else {
                        $id = "RECORD".$key;
                        //項目名
                        $setData["RECORD_LABEL"] = "<LABEL for={$id}>".$val."</LABEL>";
                        $fieldsize .= "RECACT".$csvfieldidx."=1=1=".$val."(".$settxt."),";
                        //チェックボックス
                        $check1 = ($Row["RECORD"][$key] == "1") ? "checked" : "";
                        $extra = $check1." id={$id}";
                        $setData["RECORD_VALUE"] = knjCreateCheckBox($objForm, "RECORD".$key, "1", $extra, "");
                    }
                    $arg["data"][] = $setData;
                    $csvfieldidx++;
                }
            }
        }

        //統合的な学習
        $arg["disptotalstdyflg"] = "";
        $dispchktotalstdy = $dispflgsetting[intval($gradeCd)-1]["totalstdy"];
        if ($dispchktotalstdy > 0) {
            //データ取得
            $rettxt = array();
            $rettxt = $db->getRow(knjd139iQuery::getTotalStudyText($model), DB_FETCHMODE_ASSOC);

            $arg["disptotalstdyflg"] = "1";
            $displen = $setdispinfo[intval($gradeCd)-1]["totalstdy"]["title"]["col"];
            $disprow = $setdispinfo[intval($gradeCd)-1]["totalstdy"]["title"]["row"];
            $extra = "id=\"TOTALSTUDY_TITLE\"";
            $tmpsettxt = get_count($model->warning) != 0 ? $model->field["TOTALSTUDY_TITLE"] : $rettxt["TITLE"];
            $arg["text"]["totalstudy_title"] = knjCreateTextBox($objForm, $tmpsettxt, "TOTALSTUDY_TITLE", $displen*2, $displen*2, $extra);
            $fieldsize .= "TOTALSTUDY_TITLE=".$disprow."=".$displen."="."総合的な学習(学習内容),";
            knjCreateHidden($objForm, "TOTALSTUDY_TITLE_KETA", $displen*2);
            knjCreateHidden($objForm, "TOTALSTUDY_TITLE_GYO", $disprow);
            KnjCreateHidden($objForm, "TOTALSTUDY_TITLE_STAT", "statusarea1");

            $displen = $setdispinfo[intval($gradeCd)-1]["totalstdy"]["data"]["col"];
            $disprow = $setdispinfo[intval($gradeCd)-1]["totalstdy"]["data"]["row"];
            $extra = "id=\"TOTALSTUDY_EVAL\"";
            $tmpsettxt = get_count($model->warning) != 0 ? $model->field["TOTALSTUDY_EVAL"] : $rettxt["TEXT01"];
            $arg["text"]["totalstudy_eval"] = knjCreateTextArea($objForm, "TOTALSTUDY_EVAL", $disprow, $displen, "", $extra, $tmpsettxt);
            $fieldsize .= "TOTALSTUDY_DETAIL=".$disprow."=".$displen."=総合的な学習(評価),";
            knjCreateHidden($objForm, "TOTALSTUDY_EVAL_KETA", $displen);
            knjCreateHidden($objForm, "TOTALSTUDY_EVAL_GYO", $disprow);
            KnjCreateHidden($objForm, "TOTALSTUDY_EVAL_STAT", "statusarea2");
        }

        //道徳
        $arg["dispmoralflg"] = "";
        $dispchkmoral = $dispflgsetting[intval($gradeCd)-1]["moral"];
        if ($dispchkmoral > 0) {
            //データ取得
            $rettxt = array();
            $rettxt = $db->getRow(knjd139iQuery::getMoralText($model), DB_FETCHMODE_ASSOC);

            $arg["dispmoralflg"] = "1";
            $displen = $setdispinfo[intval($gradeCd)-1]["moral"]["title"]["col"];
            $disprow = $setdispinfo[intval($gradeCd)-1]["moral"]["title"]["row"];
            $extra = "id=\"MORAL_TITLE\"";
            $tmpsettxt = get_count($model->warning) != 0 ? $model->field["MORAL_TITLE"] : $rettxt["TITLE"];
            $arg["text"]["moral_title"] = knjCreateTextBox($objForm, $tmpsettxt, "MORAL_TITLE", $displen*2, $displen*2, $extra);
            $fieldsize .= "MORAL_TITLE=".$disprow."=".$displen."="."道徳(教材),";
            knjCreateHidden($objForm, "MORAL_TITLE_KETA", $displen*2);
            knjCreateHidden($objForm, "MORAL_TITLE_GYO", $disprow);
            KnjCreateHidden($objForm, "MORAL_TITLE_STAT", "statusarea3");

            $displen = $setdispinfo[intval($gradeCd)-1]["moral"]["data"]["col"];
            $disprow = $setdispinfo[intval($gradeCd)-1]["moral"]["data"]["row"];
            $extra = "id=\"MORAL_EVAL\"";
            $tmpsettxt = get_count($model->warning) != 0 ? $model->field["MORAL_EVAL"] : $rettxt["TEXT01"];
            $arg["text"]["moral_eval"] = knjCreateTextArea($objForm, "MORAL_EVAL", $disprow, $displen, "", $extra, $tmpsettxt);
            $fieldsize .= "MORAL_DETAIL=".$disprow."=".$displen."=道徳(評価),";
            knjCreateHidden($objForm, "MORAL_EVAL_KETA", $displen);
            knjCreateHidden($objForm, "MORAL_EVAL_GYO", $disprow);
            KnjCreateHidden($objForm, "MORAL_EVAL_STAT", "statusarea4");
        }

        //特別活動・クラブ活動
        $arg["dispactclbflg"] = "";
        $dispchkactclb = $dispflgsetting[intval($gradeCd)-1]["act_club"];
        if ($dispchkactclb > 0) {
            //データ取得
            $rettxt = array();
            $rettxt = $db->getRow(knjd139iQuery::getActClubText($model), DB_FETCHMODE_ASSOC);

            $arg["dispactclbflg"] = "1";
            $displen = $setdispinfo[intval($gradeCd)-1]["act_club"]["clubfst"]["col"];
            $disprow = $setdispinfo[intval($gradeCd)-1]["act_club"]["clubfst"]["row"];
            $extra = "id=\"CLUB_FIRST\"";
            $tmpsettxt = get_count($model->warning) != 0 ? $model->field["CLUB_FIRST"] : $rettxt["TEXT01"];
            $arg["text"]["club_first"] = knjCreateTextArea($objForm, "CLUB_FIRST", $disprow, $displen, "", $extra, $tmpsettxt);
            $fieldsize .= "ACTCLB_1ST=".$disprow."=".$displen."="."特別活動・クラブ活動(部活動_前期),";
            knjCreateHidden($objForm, "CLUB_FIRST_KETA", $displen);
            knjCreateHidden($objForm, "CLUB_FIRST_GYO", $disprow);
            KnjCreateHidden($objForm, "CLUB_FIRST_STAT", "statusarea5");

            $displen = $setdispinfo[intval($gradeCd)-1]["act_club"]["clubsnd"]["col"];
            $disprow = $setdispinfo[intval($gradeCd)-1]["act_club"]["clubsnd"]["row"];
            $extra = "id=\"CLUB_SCND\"";
            $tmpsettxt = get_count($model->warning) != 0 ? $model->field["CLUB_SCND"] : $rettxt["TEXT02"];
            $arg["text"]["club_scnd"] = knjCreateTextArea($objForm, "CLUB_SCND", $disprow, $displen, "", $extra, $tmpsettxt);
            $fieldsize .= "ACTCLB_2ND=".$disprow."=".$displen."="."特別活動・クラブ活動(部活動_後期),";
            knjCreateHidden($objForm, "CLUB_SCND_KETA", $displen);
            knjCreateHidden($objForm, "CLUB_SCND_GYO", $disprow);
            KnjCreateHidden($objForm, "CLUB_SCND_STAT", "statusarea6");

            $displen = $setdispinfo[intval($gradeCd)-1]["act_club"]["favorite"]["col"];
            $disprow = $setdispinfo[intval($gradeCd)-1]["act_club"]["favorite"]["row"];
            $extra = "id=\"FAVORITE_ACTV\"";
            $tmpsettxt = get_count($model->warning) != 0 ? $model->field["FAVORITE_ACTV"] : $rettxt["TEXT03"];
            $arg["text"]["favorite_actv"] = knjCreateTextArea($objForm, "FAVORITE_ACTV", $disprow, $displen, "", $extra, $tmpsettxt);
            $fieldsize .= "ACTCLB_3RD=".$disprow."=".$displen."="."特別活動・クラブ活動(クラブ活動),";
            knjCreateHidden($objForm, "FAVORITE_ACTV_KETA", $displen);
            knjCreateHidden($objForm, "FAVORITE_ACTV_GYO", $disprow);
            KnjCreateHidden($objForm, "FAVORITE_ACTV_STAT", "statusarea7");
        }

        //出欠・特記事項
        $arg["dispremarkflg"] = "";
        $dispchkactclb = $dispflgsetting[intval($gradeCd)-1]["other"];
        if ($dispchkactclb > 0) {
            //データ取得
            $rettxt = array();
            $rettxt = $db->getRow(knjd139iQuery::getRemarkText($model), DB_FETCHMODE_ASSOC);

            //タイトル行の処理
            $arg["dispremarkttlflg"] = "";
            if ($dispflgsetting[intval($gradeCd)-1]["otherttl"] > 0) {
                $arg["dispremarkttlflg"] = "1";
                $displen = $setdispinfo[intval($gradeCd)-1]["other"]["title"]["col"];
                $disprow = $setdispinfo[intval($gradeCd)-1]["other"]["title"]["row"];
                $extra = "id=\"REMARK_TCTTL\"";
                $tmpsettxt = get_count($model->warning) != 0 ? $model->field["REMARK_TCTTL"] : $rettxt["TEXT03"];
                $arg["text"]["remark_tcttl"] = knjCreateTextArea($objForm, "REMARK_TCTTL", $disprow, $displen, "", $extra, $tmpsettxt);
                $fieldsize .= "REMARK_TCTITLE=".$disprow."=".$displen."="."特記事項,";
                knjCreateHidden($objForm, "REMARK_TCTTL_KETA", $displen);
                knjCreateHidden($objForm, "REMARK_TCTTL_GYO", $disprow);
                KnjCreateHidden($objForm, "REMARK_TCTTL_STAT", "statusarea10");
            }

            $arg["dispremarkflg"] = "1";
            $displen = $setdispinfo[intval($gradeCd)-1]["other"]["attend"]["col"];
            $disprow = $setdispinfo[intval($gradeCd)-1]["other"]["attend"]["row"];
            $extra = "id=\"REMARK_ATTEND\"";
            $tmpsettxt = get_count($model->warning) != 0 ? $model->field["REMARK_ATTEND"] : $rettxt["TEXT01"];
            $arg["text"]["remark_attend"] = knjCreateTextArea($objForm, "REMARK_ATTEND", $disprow, $displen, "", $extra, $tmpsettxt);
            $fieldsize .= "REMARK_ATTEND=".$disprow."=".$displen."="."出欠の備考,";
            knjCreateHidden($objForm, "REMARK_ATTEND_KETA", $displen);
            knjCreateHidden($objForm, "REMARK_ATTEND_GYO", $disprow);
            KnjCreateHidden($objForm, "REMARK_ATTEND_STAT", "statusarea8");

            $displen = $setdispinfo[intval($gradeCd)-1]["other"]["remark"]["col"];
            $disprow = $setdispinfo[intval($gradeCd)-1]["other"]["remark"]["row"];
            $extra = "id=\"REMARK_TEACHERCOMMENT\"";
            $tmpsettxt = get_count($model->warning) != 0 ? $model->field["REMARK_TEACHERCOMMENT"] : $rettxt["TEXT02"];
            $arg["text"]["remark_teachercomment"] = knjCreateTextArea($objForm, "REMARK_TEACHERCOMMENT", $disprow, $displen, "", $extra, $tmpsettxt);
            $fieldsize .= "REMARK_TCDETAIL=".$disprow."=".$displen."="."担任からの通信,";
            knjCreateHidden($objForm, "REMARK_TEACHERCOMMENT_KETA", $displen);
            knjCreateHidden($objForm, "REMARK_TEACHERCOMMENT_GYO", $disprow);
            KnjCreateHidden($objForm, "REMARK_TEACHERCOMMENT_STAT", "statusarea9");
        }

        //警告メッセージがある時と、更新の際はモデルの値を参照する
        if (isset($model->warning)) {
            $Row =& $model->record;
            $row =& $model->field;
        }

        //更新ボタン
        $extra = "id=\"btn_update\" onclick=\"return btn_submit('update')\"";
        $arg["button"]["btn_update"] = KnjCreateBtn($objForm, "btn_update", "更 新", $extra);

        //更新後前の生徒へ/更新後次の生徒へボタン
        $arg["button"]["btn_up_next"] = View::updateNext2($model, $objForm, $model->schregno, "SCHREGNO", "updEdit", "update");

        //取消ボタン
        $extra = "onclick=\"return btn_submit('clear')\"";
        $arg["button"]["btn_reset"] = KnjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_back"] = KnjCreateBtn($objForm, "btn_back", "終 了", $extra);

        //出欠備考参照ボタン
        $extra = "onclick=\"return btn_submit('attendRemark');\"";
        $arg["button"]["btn_attendRemark"] = knjCreateBtn($objForm, "btn_attendRemark", "出欠備考参照", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
        knjCreateHidden($objForm, "HID_GRADE", intval($gradeCd));
        knjCreateHidden($objForm, "HID_RECACTVIDLIST", implode(",", $recactvidlist));
        knjCreateHidden($objForm, "HID_RECACTVNAMELIST", implode(",", $recactvnamelist));

        knjCreateHidden($objForm, "PARAMYEAR", $model->exp_year);
        knjCreateHidden($objForm, "PARAMGRADE", $model->grade);
        knjCreateHidden($objForm, "PARAMHRCLASS", $model->hrclass);
        knjCreateHidden($objForm, "FIELDSIZE", $fieldsize);

        //CSVボタン
        $extra = "onClick=\"myBtnWopen()\"";
        $arg["button"]["btn_csv"] = KnjCreateBtn($objForm, "btn_csv", "データCSV", $extra);

        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();

        //DB切断
        Query::dbCheckIn($db);
        if (get_count($model->warning) == 0 && $model->cmd != "clean") {
            $arg["NOT_WARNING"] = 1;
            $arg["next"] = "NextStudent2(0);";
        } elseif ($model->cmd == "clean") {
            $arg["NOT_WARNING"] = 1;
            $arg["next"] = "NextStudent2(1);";
        }
        //画面のリロード
        if (get_count($model->warning) == 0 && $model->cmd == "updEdit") {
            $arg["reload"] = "parent.left_frame.btn_submit('list');";
        }

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML5($model, "knjd139iForm1.html", $arg);
    }

    //コンボ作成
    public function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
    {
        $opt = array();
        if ($blank != "") {
            $opt[] = array("label" => "", "value" => "");
        }
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($value == $row["VALUE"]) {
                $value_flg = true;
            }
        }

        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
        $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

        $result->free();
    }
}
