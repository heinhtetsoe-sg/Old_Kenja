<?php

require_once('for_php7.php');

class knjz430form
{
    public function main(&$model)
    {
        $objForm = new form();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjz430index.php", "", "right_list");

        $db = Query::dbCheckOut();

        $arg["YEAR"]        = $model->year;
        $arg["YEAR_ADD"]    = $model->year_add;

        //ALLチェック
        $arg["CHECKALL"] = $this->createCheckBox($objForm, "CHECKALL", "", "onClick=\"return check_all(this);\"", "");

        //データ作成
        $this->makeData($objForm, $arg, $db, $model);

        //実行ボタンを作成する
        $arg["btn_execute"] = $this->createBtn($objForm, "btn_execute", "実 行", "onclick=\"return btn_submit('execute');\"");

        //削除ボタンを作成する
        $arg["btn_end"] = $this->createBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");

        //hiddenを作成する
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "YEAR", $model->year);
        knjCreateHidden($objForm, "YEAR_ADD", $model->year_add);

        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz430Form.html", $arg);
    }

    //
    public function makeData(&$objForm, &$arg, $db, $model)
    {
        $setval = array();  //出力データ配列
        $getData = array();
        $getData = $this->setData($model);
        $disabled = "";
        for ($i = 0; $i < get_count($getData); $i++) {
            if ($getData[$i]["VALUE"] == "CERTIF_KIND_YDAT") {
                $tablelist = array("CERTIF_KIND_YDAT", "CERTIF_SCHOOL_DAT");
                $kekka = "";
                foreach ($tablelist as $table) {
                    $query = knjz430Query::cntTable($table);
                    $exist_flg = $db->getOne($query) > 0 ? true : false;
                    if ($exist_flg) {
                        $kekka   = ($kekka == "") ? $db->getOne(knjz430Query::getkekka($model->year, $model->year_add, $table)) : $kekka;
                    } else {
                        $kekka   = ($kekka == "") ? '今年度データなし' : $kekka;
                    }
                }
                $setval["KEKKA"] = $kekka;
            } elseif ($getData[$i]["VALUE"] == "ATTEND_SUBCLASS_SPECIAL_DAT") {
                //科目年度データチェック（次年度）
                $count = $db->getOne(knjz430Query::getCount($model->year_add, "SUBCLASS_YDAT"));

                $query = knjz430Query::cntTable($getData[$i]["VALUE"]);
                $exist_flg = $db->getOne($query) > 0 ? true : false;
                if ($count == 0) {
                    $setval["KEKKA"]   = '事前処理なし：科目マスタ';
                } elseif ($exist_flg) {
                    $setval["KEKKA"]   = $db->getOne(knjz430Query::getkekka($model->year, $model->year_add, $getData[$i]["VALUE"]));
                } else {
                    $setval["KEKKA"]   = '今年度データなし';
                }
            } elseif ($getData[$i]["VALUE"] == "CREDIT_SPECIAL_MST") {
                //在籍データチェック（次年度）
                $count = $db->getOne(knjz430Query::getCount($model->year_add, "SCHREG_REGD_DAT"));

                $query = knjz430Query::cntTable($getData[$i]["VALUE"]);
                $exist_flg = $db->getOne($query) > 0 ? true : false;
                if ($count == 0) {
                    $setval["KEKKA"]   = '事前処理なし：在籍データ';
                } elseif ($exist_flg) {
                    $setval["KEKKA"]   = $db->getOne(knjz430Query::getkekka($model->year, $model->year_add, $getData[$i]["VALUE"]));
                } else {
                    $setval["KEKKA"]   = '今年度データなし';
                }
            } elseif ($getData[$i]["VALUE"] == "HOLIDAY_MST") {
                //祝祭日マスタチェック（次年度）
                $query = knjz430Query::cntTable($getData[$i]["VALUE"]);
                $exist_flg = $db->getOne($query) > 0 ? true : false;
                if ($exist_flg) {
                    $setval["KEKKA"]   = $db->getOne(knjz430Query::getkekka($model->year, $model->year_add, $getData[$i]["VALUE"], " FISCALYEAR(HOLIDAY) "));
                } else {
                    $setval["KEKKA"]   = '今年度データなし';
                }
            } else {
                $query = knjz430Query::cntTable($getData[$i]["VALUE"]);
                $exist_flg = $db->getOne($query) > 0 ? true : false;
                if ($exist_flg) {
                    $setval["KEKKA"]   = $db->getOne(knjz430Query::getkekka($model->year, $model->year_add, $getData[$i]["VALUE"]));
                } else {
                    $setval["KEKKA"]   = '今年度データなし';
                }
            }

            $setval["MSTNAME"] = $getData[$i]["NAME"];
            if ($setval["KEKKA"] != "") {
                $disabled = "disabled";
            } else {
                $disabled = "";
            }
            $setval["CHECKED"] = $this->createCheckBox($objForm, "CHECKED", $getData[$i]["VALUE"], $disabled, "1");
            $arg["data"][] = $setval;
        }
    }

    //表示用データ配列作成
    public function setData($model)
    {
        $setD = array();
        $setD[] = (array("NAME" => "学校マスタ",                          "VALUE" => "SCHOOL_MST"));
        $setD[] = (array("NAME" => "学期マスタ",                          "VALUE" => "SEMESTER_MST"));
        $setD[] = (array("NAME" => "学期詳細マスタ",                      "VALUE" => "SEMESTER_DETAIL_MST"));
        $setD[] = (array("NAME" => "課程マスタ",                          "VALUE" => "COURSE_YDAT"));
        $setD[] = (array("NAME" => "学科マスタ",                          "VALUE" => "MAJOR_YDAT"));
        $setD[] = (array("NAME" => "コースマスタ",                        "VALUE" => "COURSECODE_YDAT"));
        $setD[] = (array("NAME" => "教科マスタ",                          "VALUE" => "CLASS_YDAT"));
        $setD[] = (array("NAME" => "科目マスタ",                          "VALUE" => "SUBCLASS_YDAT"));
        $setD[] = (array("NAME" => "出身学校マスタ",                      "VALUE" => "FINSCHOOL_YDAT"));
        $setD[] = (array("NAME" => "塾マスタ",                            "VALUE" => "PRISCHOOL_YDAT"));
        $setD[] = (array("NAME" => "名称マスタ",                          "VALUE" => "NAME_YDAT"));
        $setD[] = (array("NAME" => "施設マスタ",                          "VALUE" => "FACILITY_YDAT"));
        $setD[] = (array("NAME" => "教科書マスタ",                        "VALUE" => "TEXTBOOK_YDAT"));
        $setD[] = (array("NAME" => "教科書発行社マスタ",                  "VALUE" => "ISSUECOMPANY_YDAT"));
        $setD[] = (array("NAME" => "証明書マスタ（証明書学校データ含む）","VALUE" => "CERTIF_KIND_YDAT"));
        $setD[] = (array("NAME" => "所属マスタ",                          "VALUE" => "SECTION_YDAT"));
        $setD[] = (array("NAME" => "職名マスタ",                          "VALUE" => "JOB_YDAT"));
        $setD[] = (array("NAME" => "職員マスタ",                          "VALUE" => "STAFF_YDAT"));
        $setD[] = (array("NAME" => "学年名称マスタ",                      "VALUE" => "SCHREG_REGD_GDAT"));
        $setD[] = (array("NAME" => "グループマスタ",                      "VALUE" => "USERGROUP_DAT"));
        $setD[] = (array("NAME" => "テスト名称マスタ",                    "VALUE" => "TESTITEM_MST_COUNTFLG_NEW"));
        $setD[] = (array("NAME" => "特別活動グループマスタ",              "VALUE" => "ATTEND_SUBCLASS_SPECIAL_DAT"));
        $setD[] = (array("NAME" => "単位マスタ（法定授業特別活動設定）",  "VALUE" => "CREDIT_SPECIAL_MST"));
        $setD[] = (array("NAME" => "出欠コードマスタ",                    "VALUE" => "ATTEND_DI_CD_DAT"));
        $setD[] = (array("NAME" => "PRG別印影表示欄設定",                 "VALUE" => "PRG_STAMP_DAT"));
        $setD[] = (array("NAME" => "科目合併設定",                        "VALUE" => "SUBCLASS_REPLACE_COMBINED_DAT"));
        $setD[] = (array("NAME" => "部活マスタ",                          "VALUE" => "CLUB_YDAT"));
        $setD[] = (array("NAME" => "部活動顧問マスタ",                    "VALUE" => "CLUB_ADVISER_DAT"));
        $setD[] = (array("NAME" => "委員会マスタ",                        "VALUE" => "COMMITTEE_YDAT"));
        $setD[] = (array("NAME" => "委員会顧問マスタ",                    "VALUE" => "COMMITTEE_ADVISER_DAT"));
        $setD[] = (array("NAME" => "教務主任等マスタ",                    "VALUE" => "POSITION_YDAT"));
        if ($model->z010Name == "meikei") {
            $setD[] = (array("NAME" => "寮マスタ",                        "VALUE" => "DOMITORY_YDAT"));
        }
        $setD[] = (array("NAME" => "単位マスタ",                          "VALUE" => "CREDIT_MST"));
        $setD[] = (array("NAME" => "祝祭日マスタ",                        "VALUE" => "HOLIDAY_MST"));

        return $setD;
    }

    //チェックボックス作成
    public function createCheckBox(&$objForm, $name, $value, $extra, $multi)
    {
        $objForm->ae(array("type"      => "checkbox",
                            "name"      => $name,
                            "value"     => $value,
                            "extrahtml" => $extra,
                            "multiple"  => $multi));

        return $objForm->ge($name);
    }

    //ボタン作成
    public function createBtn(&$objForm, $name, $value, $extra)
    {
        $objForm->ae(array("type"        => "button",
                            "name"        => $name,
                            "extrahtml"   => $extra,
                            "value"       => $value ));
        return $objForm->ge($name);
    }

    //Hidden作成ae
    public function createHiddenAe($name, $value = "")
    {
        $opt_hidden = array();
        $opt_hidden = array("type"      => "hidden",
                            "name"      => $name,
                            "value"     => $value);
        return $opt_hidden;
    }
}
