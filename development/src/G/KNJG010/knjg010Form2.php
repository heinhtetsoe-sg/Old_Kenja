<?php

require_once('for_php7.php');


// kanji=漢字

class knjg010Form2
{
    public function main(&$model)
    {
        $objForm = new form();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjg010Form2", "POST", "knjg010index.php", "", "knjg010Form2");

        //年度
        $arg["YEAR"] = $model->year;

        //表示
        //表示選択コンボボックスを作成する
        $model->disp = isset($model->disp) ? $model->disp : 1;

        //表示種別コンボの設定
        $opt = array();
        $opt[0]= array("label" => "未発行の依頼", "value" => 1);
        $opt[1]= array("label" => "全ての依頼",   "value" => 2);
        $opt[2]= array("label" => "発行済み（卒業生）",   "value" => 3);
        $opt[3]= array("label" => "発行済み（在学生）",   "value" => 4);

        $objForm->ae(array("type"       => "select",
                            "name"       => "DISP",
                            "size"       => "1",
                            "value"      => $model->disp,
                            "extrahtml"  => "Onchange=\"btn_submit('init')\"",
                            "options"    => $opt));

        $arg["DISP"] = $objForm->ge("DISP");

        $opt2 = array();
        $opt2[0] = array('label' => '学籍番号　 年組　　氏　　名 　　証明書種類'  , 'value' => 'DUMMY0');
        $opt2[1] = array('label' => '－－－－－－－－－－－－－－－－－－－－－－', 'value' => 'DUMMY1');

        if ($model->cmd == "list3") {
            for ($i = 0; $i < get_count($model->issue_data); $i++) {
                $work = explode(",", $model->issue_data[$i]);
                if ($work[24] == "0") {
                    continue;
                }
                $strwork2 = "　　" .$work[0] ." " .$work[15] ."　" .$work[14];
                $val = implode(",", array(
                      $work[0],     //学籍番号    0 コード
                      $work[1],     //帳票種別    1 コード
                      $work[2],     //年度        2 コード
                      $work[3],     //学期        3 コード
                      $work[4],     //学年        4 コード
                      $work[5],     //記載責任者  5 コード
                      $work[6],     //評定の読替  6 コード
                      $work[7],     //漢字出力    7 コード
                      $work[8],     //処理日付    8 日付
                      $work[9],     //証明発行番号9 コード
                      $work[10],    //事務出力区分名10 名前
                      $work[11],    //発行区分    11 名前
                      $work[12],    //クラス      12 コード
                      $work[13],    //帳票種別名  13 名前
                      $work[14],    //姓名        14 名前
                      $work[15],    //クラス名称  15 名前
                      $work[16],    //生年月日    16 日付
                      $work[17],    //課程        17 名前
                      $work[18],    //学科        18 名前
                      $work[19],    //卒業年月日  19 日付
                      $work[20],    //申請年月日  20 日付
                      $work[21],    //証明申請番号21 コード
                      $work[22],    //発行日付    22 日付
                      $work[23],    //既卒区分    23
                      $work[24],    //事務出力区分24 コード
                      $work[25],    //HR担任者名  25 名前
                      $work[26],    //卒業期　　  26
                      $work[27],    //発行区分　  27
                      $work[28],    //発行番号手入力　  28
                      $work[29],    //概評情報　  29
                      $work[30],    //その他住所　  30
                      $work[31],    //前籍校を含まない　  31
                      $work[32],    //フォーム選択　  32
                      $work[33],    //履修のみ科目出力　  33
                      $work[34],    //未履修科目出力　  34
                      $work[35],    //CERTIF_DETAIL_EACHTYPE_DATの証明番号(パラメータデータの有無チェック用)　  35
                      $work[36],    //入学・卒業日付は年月で表示する　  36
                      $work[37],    //キャンセル 37
                      $work[38],    //印影出力 38
                      $work[39],    //半期認定フォーム出力 39
                      $work[40],    //留学の単位数を0表示 40
                      $work[41],    //総合的な学習の時間の単位数を0表示 41
                      $work[42],    //校長印 42
                      $work[43],    //担任印 43
                      $work[44],    //偶数頁に生徒名出力 44
                      $work[45],    //評定平均算出 45
                      $work[46],    //評定平均席次 46
                      $work[47]     //ページ出力選択 47
                  ));

                $opt2[$i + 2] = array("label" => $strwork2, "value" => $val);
            }
            $arg["reload"]  = "btn_issuesubmit('print2','" . SERVLET_URL . "');";
        }

        $opt1Array = array();
        $opt1Array["JKUBUN"] = "印刷";
        if ($model->cmd == "list2") {
            $opt1Array["CERTIF_NO"] = "番号";
        } else {
            $opt1Array["HKUBUN"] = "発行";
        }
        $opt1Array["HR_NAME"] = "　　年組";
        $opt1Array["SCHREGNO"] = "　学籍番号";
        $opt1Array["APPLYDATE"] = "　申請年月日";
        $opt1Array["KINDNAME"] = "　　証明書種類";
        $opt1Array["NAME_SHOW"] = "　氏　　名";
        $opt1Array["GYEAR"] = "　卒業期";
        $opt1Array["CANCEL"] = "キャンセル";
        $opt1Array["REMARK1"] = "発行番号";

        $db     = Query::dbCheckOut();
        $fieldHaba = knjg010Query::getMainDataHaba($db, $model);
        foreach ($opt1Array as $field => $title) {
            if ($fieldHaba[$field] == '' || $fieldHaba[$field] < mb_strwidth($title)) { // 各フィールド幅とタイトルの幅を比較
                $fieldHaba[$field] = mb_strwidth($title);
            }
            if ($fieldHaba[$field] % 2 == 1) {
                $fieldHaba[$field] += 1;
            }
        }
        $opt1 = array();
        $opt1[0] = "";
        foreach ($opt1Array as $field => $title) {
            $opt1[0] .= "　".addSpace($title, $fieldHaba[$field]);
        }
        $opt1[1] .= addSpace("", mb_strwidth($opt1[0]), "", "－");
        $query  = knjg010Query::getMainData($model);
        $result = $db->query($query);
        for ($j = 2; $row = $result->fetchRow(DB_FETCHMODE_ASSOC); $j++) {
            if ($model->cmd == "list3") {
                for ($i = 1; $i <= get_count($model->issue_data); $i++) {
                    $work = explode(",", $model->issue_data[$i]);
                    if ($work[0] == $row["SCHREGNO"] && $work[0] == $row["CERTIF_INDEX"] && $work[24] != "0") {
                        $i = 0;
                        break;
                    }
                }
                if ($i == 0) {
                    continue;
                }
            }
            $strwork = "";
            $strwork .= "　" .addSpace($row["JKUBUN"], $fieldHaba["JKUBUN"]);
            if ($model->cmd == "list2") {
                $strwork .= "　" .addSpace($row["CERTIF_NO"], $fieldHaba["CERTIF_NO"]);
            } else {
                $strwork .= "　" .addSpace($row["HKUBUN"], $fieldHaba["HKUBUN"]);
            }
            $strwork .= "　" .addSpace($row["HR_NAME"], $fieldHaba["HR_NAME"]);
            $strwork .= "　" .addSpace($row["SCHREGNO"], $fieldHaba["SCHREGNO"]);
            $strwork .= "　" .addSpace(str_replace("-", "/", $row["APPLYDATE"]), $fieldHaba["APPLYDATE"]);
            $strwork .= "　" .addSpace(trim($row["KINDNAME"]), $fieldHaba["KINDNAME"]);
            $strwork .= "　" .addSpace($row["NAME_SHOW"], $fieldHaba["NAME_SHOW"], "NAME_SHOW");
            $strwork .= "　" .addSpace($row["GYEAR"], $fieldHaba["GYEAR"], "GYEAR");
            $strwork .= "　" .addSpace(($row["CANCEL_FLG"] == "1" ? "キャンセル" : ""), $fieldHaba["CANCEL"], "CANCEL");
            $strwork .= "　" .addSpace(($row["REMARK1"] ? $row["REMARK1"] : $row["CERTIF_NO"]), $fieldHaba["REMARK1"]);

            $val = implode(",", array(
                   $row["SCHREGNO"],          //学籍番号    0
                   $row["CERTIF_KINDCD"],     //帳票種別    1
                   $row["YEAR"],              //年度        2
                   $row["SEMESTER"],          //学期        3
                   $row["GRADE"],             //学年        4
                   $row["TR_CD1"],             //記載責任者  5
                   $row["HYOUTEI"],            //評定の読替  6
                   $row["KJ_OUT"],             //漢字出力    7
                   $row["PR_DATE"],            //処理日付    8
                   $row["CERTIF_NO"],         //証明番号    9
                   $row["JKUBUN"],            //事務出力区分名10
                   $row["HKUBUN"],            //発行区分名  11
                   $row["HR_CLASS"],          //クラス      12
                   $row["KINDNAME"],          //帳票種別名  13
                   $row["NAME_SHOW"],         //姓名        14
                   $row["HR_NAME"],           //クラス名称  15
                   str_replace("-", "/", $row["BIRTHDAY"]),   //生年月日    16
                   $row["COURSENAME"],        //課程        17
                   $row["MAJORNAME"],         //学科        18
                   str_replace("-", "/", $row["GRD_DATE"]),    //卒業年月日  19
                   str_replace("-", "/", $row["APPLYDATE"]),       //申請年月日  20
                   $row["CERTIF_INDEX"],      //証明番号    21
                   $row["ISSUEDATE"],         //発行日付    22
                   $row["GRADUATE_FLG"],         //既卒区分    23
                   $row["J_ISSUECD"],          //事務出力区分24
                   $row["STAFFNAME"],         //HR担任者名25
                   $row["GYEAR"],             //卒業期　　  26
                   $row["ISSUECD"],                //発行区分    27
                   $row["REMARK1"],                //発行番号手入力　  28
                   $row["GAIHYOU"],         //概評情報　  29
                   $row["SONOTAJUUSYO"],    //その他住所　  30
                   $row["REMARK11"],         //前籍校を含まない　  31
                   $row["FORM6"],           //フォーム選択　  32
                   $row["RISYU"],           //履修のみ科目出力　  33
                   $row["MIRISYU"],         //未履修科目出力　  34
                   $row["D_CERTIF_INDEX"],       //CERTIF_DETAIL_EACHTYPE_DATの証明番号(パラメータデータの有無チェック用)　  35
                   $row["ENT_GRD_DATE_FORMAT"],   //入学・卒業日付は年月で表示する　  36
                   $row["CANCEL_FLG"],             //キャンセル　  37
                   $row["PRINT_STAMP"],               //印影出力　  38
                   $row["HANKI_NINTEI_FORM"],      //半期認定フォーム出力　  39
                   $row["RYUGAKU_CREDIT"],         //留学の単位数を0表示　  40
                   $row["SOGAKU_CREDIT"],             //総合的な学習の時間の単位数を0表示　  41
                   $row["KNJE070_CHECK_PRINT_STAMP_PRINCIPAL"],         //校長印  42
                   $row["KNJE070_CHECK_PRINT_STAMP_HR_STAFF"],             //担任印  43
                   $row["KNJE070D_PRINTHEADERNAME"],             // 偶数頁に氏名出力  44
                   $row["GVAL_CALC_CHECK"],  // 評定平均算出 45
                   $row["PRINT_AVG_RANK"],  // 評定平均席次 46
                   $row["tyousasyo2020shojikouExtendsSelect"]  // 評定平均席次 47
               ));

            $opt1[$j] =  array("label" => htmlspecialchars($strwork),
                               "value" => $val);
        }
        //発行日付　パラメータなし用
        knjCreateHidden($objForm, "SET_PR_DATE", str_replace("-", "/", $model->control["学籍処理日"]));

        $opt1[0] = array('label' => $opt1[0], 'value' => "DUMMY0");
        $opt1[1] = array('label' => $opt1[1], 'value' => 'DUMMY1');

        //一覧リストを作成する
        $objForm->ae(array("type"       => "select",
                            "name"       => "category_selected",
                            "extrahtml"  => "multiple STYLE=\"WIDTH:100%;font-family:monospace;height:200px\" onChange=\"issue_select2()\" ondblclick=\"lmove('left','$model->cmd')\"",
                            "size"       => "20",
                            "options"    => $opt1));

        $arg["data"]["CATEGORY_SELECTED"] = $objForm->ge("category_selected");

        //対象者リストを作成する
        $objForm->ae(array("type"       => "select",
                            "name"       => "category_name",
                            "extrahtml"  => "multiple STYLE=\"WIDTH:100%;font-family:monospace;height:200px\" ondblclick=\"rmove('right','$model->cmd')\"",
                            "size"       => "20",
                            "options"    => $opt2));

        $arg["data"]["category_name"] = $objForm->ge("category_name");

        createButton($objForm, $arg, $model);

        createHidden($objForm, $model);

        if ($model->cmd == "list2" || $model->disp == "3" || $model->disp == "4") {
            // 発行済み（卒業生）or 発行済み（在学生）
            $arg["reload"] = "";
            $arg["reload1"] = "";
            $arg["reload2"] = "bottm_frm_disable2('list2');";
        } else {
            $reload = "";
            $reload .= " if (document.forms[0].cmd.value == 'list') { ";
            $reload .= "     bottm_frm_disable2('');";
            $reload .= " } ";
            $arg["reload1"] = $reload;
        }
        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjg010Form2.html", $arg);
    }
}

function createButton(&$objForm, &$arg, $model)
{

    //読込ボタンを作成する
    $arg["button"]["btn_read"]   = KnjCreateBtn($objForm, "btn_read", "読込", " onclick=\"btn_submit('init');\"");

    //対象取り消しボタンを作成する(個別)
    $arg["button"]["btn_right1"] = KnjCreateBtn($objForm, "btn_right1", "　＞　", "style=\"height:20px;width:40px\" onclick=\"rmove('right','$model->cmd');\"");

    //対象取り消しボタンを作成する(全て)
    $arg["button"]["btn_right2"] = KnjCreateBtn($objForm, "btn_right2", "　≫　", "style=\"height:20px;width:40px\" onclick=\"rmove('rightall','$model->cmd');\"");

    //対象選択ボタンを作成する(個別)
    $arg["button"]["btn_left1"]  = KnjCreateBtn($objForm, "btn_left1", "　＜　", "style=\"height:20px;width:40px\" onclick=\"lmove('left','$model->cmd');\"");

    //対象選択ボタンを作成する(全て)
    $arg["button"]["btn_left2"]  = KnjCreateBtn($objForm, "btn_left2", "　≪　", "style=\"height:20px;width:40px\" onclick=\"lmove('leftall','$model->cmd');\"");

    if ($model->cmd == "list2") {
        //印刷ボタンを作成する
        $arg["button"]["btn_issue"] = KnjCreateBtn($objForm, "btn_issue", "印　刷", " onclick=\"btn_issuesubmit('print','" . SERVLET_URL . "');\"");
    } else {
        //発行ボタンを作成する
        $arg["button"]["btn_issue"] = KnjCreateBtn($objForm, "btn_issue", "発　行", " onclick=\"btn_issuesubmit('issue','" . SERVLET_URL . "');\"");

        if ($model->disp == 1) {
            //印刷ボタンを作成する
            $arg["button"]["btn_testprint"] = KnjCreateBtn($objForm, "btn_testprint", "テスト印刷", " onclick=\"btn_issuesubmit('testprint','" . SERVLET_URL . "');\"");

            //削除ボタンを作成する
            $arg["button"]["btn_delete"] = KnjCreateBtn($objForm, "btn_delete", "削　除", " onclick=\"btn_submit('delete');\"");
        }
    }
}

function createHidden(&$objForm, $model)
{
    //hiddenを作成する
    knjCreateHidden($objForm, "PRGID", "KNJG010");
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "cmd", $model->cmd);
    knjCreateHidden($objForm, "disp", $model->disp);

    //ＯＳ選択(1:XP)・・・印刷パラメータ---2005.06.09Add
    knjCreateHidden($objForm, "OS", "1");
    knjCreateHidden($objForm, "useSyojikou3", $model->Properties["useSyojikou3"]);
    knjCreateHidden($objForm, "useSchool_KindField", $model->Properties["useSchool_KindField"]);
    knjCreateHidden($objForm, "SCHOOLKIND", SCHOOLKIND);
    knjCreateHidden($objForm, "certif_no_8keta", $model->Properties["certif_no_8keta"]);

    //近大チェック
    $schoolname = strtoupper($model->school_name);
    if (!$schoolname || !($schoolname == "KINDAI" || $schoolname == "KINJUNIOR")) {
        $not_kindai = '1';
    } else {
        $not_kindai = "";
    }

    knjCreateHidden($objForm, "NOT_KINDAI", $not_kindai);
    knjCreateHidden($objForm, "certifNoSyudou", $model->Properties["certifNoSyudou"]); //発行番号手動
    knjCreateHidden($objForm, "Knje080UseAForm", $model->Properties["Knje080UseAForm"]);
    knjCreateHidden($objForm, "seisekishoumeishoTaniPrintRyugaku", $model->Properties["seisekishoumeishoTaniPrintRyugaku"]);
    knjCreateHidden($objForm, "tyousasyoAttendrecRemarkFieldSize", $model->Properties["tyousasyoAttendrecRemarkFieldSize"]); //調査書出欠備考文字数
    knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR); //今年度---NO001
    knjCreateHidden($objForm, "RISYU"); //履修のみ科目出力(1:する,2:しない)・・・印刷パラメータ
    knjCreateHidden($objForm, "MIRISYU"); //未履修科目出力(1:する,2:しない)・・・印刷パラメータ
    knjCreateHidden($objForm, "FORM6"); //６年用フォーム選択(on:６年用フォーム)・・・印刷パラメータ
    knjCreateHidden($objForm, "ENT_GRD_DATE_FORMAT"); //入学・卒業日付は年月で表示する・・・印刷パラメータ

    knjCreateHidden($objForm, "gaihyouGakkaBetu", $model->Properties["gaihyouGakkaBetu"]);
    knjCreateHidden($objForm, "3_or_6_nenYoForm", $model->Properties["3_or_6_nenYoForm"]);
    knjCreateHidden($objForm, "train_ref_1_2_3_field_size", $model->Properties["train_ref_1_2_3_field_size"]);
    knjCreateHidden($objForm, "train_ref_1_2_3_gyo_size", $model->Properties["train_ref_1_2_3_gyo_size"]);
    knjCreateHidden($objForm, "tyousasyoSougouHyoukaNentani", $model->Properties["tyousasyoSougouHyoukaNentani"]);
    knjCreateHidden($objForm, "NENYOFORM");

    knjCreateHidden($objForm, "tyousasyoTotalstudyactFieldSize", $model->Properties["tyousasyoTotalstudyactFieldSize"]);
    knjCreateHidden($objForm, "tyousasyoTotalstudyvalFieldSize", $model->Properties["tyousasyoTotalstudyvalFieldSize"]);
    knjCreateHidden($objForm, "tyousasyoSpecialactrecFieldSize", $model->Properties["tyousasyoSpecialactrecFieldSize"]);
    knjCreateHidden($objForm, "tyousasyoTokuBetuFieldSize", $model->Properties["tyousasyoTokuBetuFieldSize"]);
    knjCreateHidden($objForm, "tyousasyoEMPTokuBetuFieldSize", $model->Properties["tyousasyoEMPTokuBetuFieldSize"]);
    knjCreateHidden($objForm, "tyousasyoKinsokuForm", $model->Properties["tyousasyoKinsokuForm"]);
    knjCreateHidden($objForm, "tyousasyoNotPrintAnotherStudyrec", $model->Properties["tyousasyoNotPrintAnotherStudyrec"]);
    knjCreateHidden($objForm, "tyousasyoNotPrintEnterGrade", $model->Properties["tyousasyoNotPrintEnterGrade"]);
    knjCreateHidden($objForm, "seisekishoumeishoNotPrintAnotherStudyrec", $model->Properties["seisekishoumeishoNotPrintAnotherStudyrec"]);
    knjCreateHidden($objForm, "tannishutokushoumeishoNotPrintAnotherStudyrec", $model->Properties["tannishutokushoumeishoNotPrintAnotherStudyrec"]);
    knjCreateHidden($objForm, "tyousasyoSyusyokuPrintGappeiTougou", $model->Properties["tyousasyoSyusyokuPrintGappeiTougou"]);

    knjCreateHidden($objForm, "TANIPRINT_SOUGOU", $model->Properties["tyousasyoTaniPrint"]);
    knjCreateHidden($objForm, "TANIPRINT_RYUGAKU", $model->Properties["tyousasyoTaniPrint"]);

    knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);
    knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
    knjCreateHidden($objForm, "useClassDetailDat", $model->Properties["useClassDetailDat"]);
    knjCreateHidden($objForm, "useAddrField2", $model->Properties["useAddrField2"]);
    knjCreateHidden($objForm, "useProvFlg", $model->Properties["useProvFlg"]);
    knjCreateHidden($objForm, "useGakkaSchoolDiv", $model->Properties["useGakkaSchoolDiv"]);
    knjCreateHidden($objForm, "useAddrField2", $model->Properties["useAddrField2"]);
    knjCreateHidden($objForm, "hyoteiYomikae", $model->Properties["hyoteiYomikae"]);
    knjCreateHidden($objForm, "useAssessCourseMst", $model->Properties["useAssessCourseMst"]);
    knjCreateHidden($objForm, "useMaruA_avg", $model->Properties["useMaruA_avg"]);
    knjCreateHidden($objForm, "tyousasyoRemarkFieldSize", $model->Properties["tyousasyoRemarkFieldSize"]);
    knjCreateHidden($objForm, "tyousasyoUseEditKinsoku", $model->Properties["tyousasyoUseEditKinsoku"]);
    knjCreateHidden($objForm, "certifPrintRealName", $model->Properties["certifPrintRealName"]);
    knjCreateHidden($objForm, "PARAM_TESTPRINT");
    knjCreateHidden($objForm, "tyousasyoHankiNintei", $model->Properties["tyousasyoHankiNintei"]);
    knjCreateHidden($objForm, "useShuryoShoumeisho", $model->Properties["useShuryoShoumeisho"]);
    knjCreateHidden($objForm, "tyousasyoCheckCertifDate", $model->Properties["tyousasyoCheckCertifDate"]);
    knjCreateHidden($objForm, "tyousasyoPrintHomeRoomStaff", $model->Properties["tyousasyoPrintHomeRoomStaff"]);
    knjCreateHidden($objForm, "tyousasyoPrintCoursecodename", $model->Properties["tyousasyoPrintCoursecodename"]);
    knjCreateHidden($objForm, "seisekishoumeishoPrintCoursecodename", $model->Properties["seisekishoumeishoPrintCoursecodename"]);
    knjCreateHidden($objForm, "tannishutokushoumeishoPrintCoursecodename", $model->Properties["tannishutokushoumeishoPrintCoursecodename"]);
    knjCreateHidden($objForm, "tyousasyoPrintChairSubclassSemester2", $model->Properties["tyousasyoPrintChairSubclassSemester2"]);
    knjCreateHidden($objForm, "tyousasyoHanasuClasscd", $model->Properties["tyousasyoHanasuClasscd"]);
    knjCreateHidden($objForm, "tannishutokushoumeishoKisaisekininsha", $model->Properties["tannishutokushoumeishoKisaisekininsha"]);
    knjCreateHidden($objForm, "tyousasyoJiritsuKatsudouRemark", $model->Properties["tyousasyoJiritsuKatsudouRemark"]);
    knjCreateHidden($objForm, "chutouKyoikuGakkouFlg", $model->Properties["chutouKyoikuGakkouFlg"]);
    knjCreateHidden($objForm, "tyousasyoSougouHyoukaNentaniPrintCombined", $model->Properties["tyousasyoSougouHyoukaNentaniPrintCombined"]);
    knjCreateHidden($objForm, "knjg010PrintGradeCdAsGrade", $model->Properties["knjg010PrintGradeCdAsGrade"]);
    knjCreateHidden($objForm, "seisekishoumeishoCreditOnlyClasscd", $model->Properties["seisekishoumeishoCreditOnlyClasscd"]);
    knjCreateHidden($objForm, "tyousasho2020PrintHeaderName", $model->Properties["tyousasho2020PrintHeaderName"]);
    knjCreateHidden($objForm, "hyoteiYomikaeRadio", $model->Properties["hyoteiYomikaeRadio"]);
}

// フィールド幅よりデータ幅が小さければ後ろにスペース追加
function addSpace($data, $haba, $field = "", $spaceCharZenkaku = "　", $spaceCharHankaku = " ")
{
    $dataHaba = mb_strwidth(trim($data));
    $spc = "";
    if ($dataHaba < $haba) {
        $diff = (int)$haba - (int)$dataHaba;
        if ($diff % 2 == 1) {
            $spc .= $spaceCharHankaku;
            $diff -= 1;
        }
        for ($di = 0; $di < $diff; $di+=2) {
            $spc .= $spaceCharZenkaku;
        }
        //echo $field ." = [".$data."] ... (".$dataHaba." / ".$haba.") add space [".$spc."]<br>";
    }
    return trim($data).$spc;
}
