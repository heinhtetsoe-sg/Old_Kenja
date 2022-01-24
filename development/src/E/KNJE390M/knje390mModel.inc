<?php

require_once('for_php7.php');
require_once('../../common/mycalendar.php');

class knje390mModel extends Model
{
    public $cmd;
    public $schregno = "";
    public $control_data = array(); //コントロールマスタデータを保持
    public $name = "";
    public $grade;
    public $hr_class;
    public $attendno;
    public $mainfield = array();
    public $subfield = array();
    public $field = array();
    public $field2 = array();
    public $field3 = array();
    public $field4 = array();
    public $control;
    public $mode;
    
    public $getYear;
    public $getRecordDiv;
    public $getRecordNo;
    public $getRecordSeq;
    public $getRelano;
    public $checked;
    public $data = array();
    public $replace = array();    //一括更新処理データ
    public $type;
    public $main_year;
    public $record_date;

    public $getPrgId;      //コールされたか
    public $sendAuth;      //親画面の権限
    public $auth;          //権限

    public function knje390mModel()
    {
    }

    public function init()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control_data);
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
        $this->finschoolcdKeta = ($this->Properties["useFinschoolcdFieldSize"] == "12" ? 12 : 7);
        if (VARS::get("PROGRAMID") != "") {
            unset($this->schregno);
            unset($this->name);
            unset($this->grade);
            unset($this->hr_class);
            unset($this->attendno);
            unset($this->mode);
            
            unset($this->main_year);
            unset($this->record_date);

            unset($this->type);
            unset($this->getYear);
            unset($this->getRecordDiv);
            unset($this->getRecordNo);
            unset($this->getRecordSeq);
            unset($this->getRelano);
            
            unset($this->getPrgId);
            unset($this->sendAuth);
            unset($this->auth);
        }
        $this->cmd = VARS::request("cmd");
        if (VARS::get("SEND_PRGID")) {
            $this->getPrgId = VARS::get("SEND_PRGID");
            $this->sendAuth = VARS::get("SEND_AUTH");
        }
        //権限
        $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;
        
        /*************** 年度と履歴日 ******************/
        $this->mainfield = array("YEAR_C"                    =>  VARS::post("YEAR_C"),                  //B の年度
                                "YEAR_A"                     =>  VARS::post("YEAR_A"),                  //D の年度
                                "YEAR_E"                     =>  VARS::post("YEAR_E"),                  //E の年度
                                "YEAR_F"                     =>  VARS::post("YEAR_F"),                  //F の年度
                                "YEAR_G"                     =>  VARS::post("YEAR_G"),                  //G の年度
                                "YEAR_H"                     =>  VARS::post("YEAR_H"),                  //H の年度
                                "RECORD_DATE_A"              =>  VARS::post("RECORD_DATE_A"),           //A 履歴日付
                                "RECORD_DATE_B"              =>  VARS::post("RECORD_DATE_B"),           //B 履歴日付
                                "RECORD_DATE_C"              =>  VARS::post("RECORD_DATE_C"),           //C 履歴日付
                                "RECORD_DATE_D"              =>  VARS::post("RECORD_DATE_D"),           //D 作成日付
                                "RECORD_DATE_E"              =>  VARS::post("RECORD_DATE_E"),           //E 作成日付
                                "RECORD_DATE_F"              =>  VARS::post("RECORD_DATE_F"),           //F 作成日付
                                "RECORD_DATE_G"              =>  VARS::post("RECORD_DATE_G"),           //G 作成日付
                                "RECORD_DATE_H"              =>  VARS::post("RECORD_DATE_H"),           //H 作成日付
                                "RECORD_DATE_I"              =>  VARS::post("RECORD_DATE_I"),           //H 作成日付
                                "WRITING_DATE_MAIN"          =>  VARS::post("WRITING_DATE_MAIN")        //作成年月日
                                );
        
        /*************** 家族構成 ***************/
        $this->subfield = array("RELANAME"              =>  VARS::post("RELANAME"),                     //氏名
                             "RELAKANA"                 =>  VARS::post("RELAKANA"),                     //氏名かな
                             "RELATIONSHIP"             =>  VARS::post("RELATIONSHIP"),                 //続柄
                             "REMARK"                   =>  VARS::post("REMARK")                        //備考
                            );

        /*************** サービス事業所検索 ***************/
        $this->searchfield = array("NAME"               =>  VARS::post("NAME"),                         //サービス事業名
                             "AREACD"                   =>  VARS::post("AREACD"),                       //圏域(コンボ表示制御用)
                             "ADDR1"                    =>  VARS::post("ADDR1"),                        //住所１
                             "ADDR2"                    =>  VARS::post("ADDR2")                         //住所２
                            );

        /*************** 基本情報 ***************/
        $this->field = array("RECORD_DATE"              =>  VARS::post("RECORD_DATE"),                  //作成年月日
                             "WRITING_DATE"             =>  VARS::post("WRITING_DATE"),                 //作成年月日
                             "RECORD_HISTORY"           =>  VARS::post("RECORD_HISTORY"),               //作成履歴
                             "RECORD_STAFFNAME"         =>  VARS::post("RECORD_STAFFNAME"),             //作成者
                             "TSUUGAKU_DIV1"            =>  VARS::post("TSUUGAKU_DIV1"),                //通学方法1
                             "TSUUGAKU_DIV2"            =>  VARS::post("TSUUGAKU_DIV2"),                //通学方法2
                             "TSUUGAKU_DIV1_REMARK"     =>  VARS::post("TSUUGAKU_DIV1_REMARK"),         //通学方法1備考
                             "TSUUGAKU_DIV2_REMARK"     =>  VARS::post("TSUUGAKU_DIV2_REMARK"),         //通学方法2備考
                             "CHALLENGED_NAMES"         =>  VARS::post("CHALLENGED_NAMES"),             //障害名等
                             "CHALLENGED_STATUS"        =>  VARS::post("CHALLENGED_STATUS"),            //実態概要･障害の特性
                             
                             "CHALLENGED_CARD_NAME"             =>  VARS::post("CHALLENGED_CARD_NAME"),         //療育手帳
                             "CHALLENGED_CARD_CLASS"            =>  VARS::post("CHALLENGED_CARD_CLASS"),        //身体障害手帳の種別
                             "CHALLENGED_CARD_RANK"             =>  VARS::post("CHALLENGED_CARD_RANK"),         //身体障害手帳の等級
                             "CHALLENGED_CARD_AREA_NAME"        =>  VARS::post("CHALLENGED_CARD_AREA_NAME"),    //身体障害手帳の障害名
                             "CHALLENGED_CARD_AREA_NAME2"       =>  VARS::post("CHALLENGED_CARD_AREA_NAME2"),   //身体障害手帳の障害名2
                             "CHALLENGED_CARD_AREA_NAME3"       =>  VARS::post("CHALLENGED_CARD_AREA_NAME3"),   //身体障害手帳の障害名3
                             "CHALLENGED_CARD_REMARK"           =>  VARS::post("CHALLENGED_CARD_REMARK"),       //精神障害者保健福祉手帳の備考
                             "CHALLENGED_CARD_CHECK_YM"         =>  VARS::post("CHALLENGED_CARD_CHECK_YM"),     //療育手帳の次回判定
                             "CHALLENGED_CARD_GRANT_FLG"        =>  VARS::post("CHALLENGED_CARD_GRANT_FLG"),    //身体障害手帳の次回認定
                             "CHALLENGED_CARD_GRANT_YM"         =>  VARS::post("CHALLENGED_CARD_GRANT_YM"),     //身体障害手帳の次回認定月
                             "CHALLENGED_CARD_BAST_YM"          =>  VARS::post("CHALLENGED_CARD_BAST_YM"),      //精神障害者保健福祉手帳の有効期限
                             
                             "WELFARE_MEDICAL_RECEIVE_FLG"      =>  VARS::post("WELFARE_MEDICAL_RECEIVE_FLG"),      //受給者証有無
                             "WELFARE_MEDICAL_RECEIVE_DIV"      =>  VARS::post("WELFARE_MEDICAL_RECEIVE_DIV"),      //種類
                             "WELFARE_MEDICAL_RECEIVE_DIV2"     =>  VARS::post("WELFARE_MEDICAL_RECEIVE_DIV2"),     //種類2
                             "WELFARE_MEDICAL_RECEIVE_DIV3"     =>  VARS::post("WELFARE_MEDICAL_RECEIVE_DIV3"),     //種類3
                             "WELFARE_MEDICAL_RECEIVE_DIV4"     =>  VARS::post("WELFARE_MEDICAL_RECEIVE_DIV4"),     //種類4
                             "WELFARE_MEDICAL_RECEIVE_DIV5"     =>  VARS::post("WELFARE_MEDICAL_RECEIVE_DIV5"),     //種類5
                             "P_SCHOOL_CD"                      =>  (VARS::post("P_SCHOOL_CD") ? sprintf("%0".($this->finschoolcdKeta)."s", VARS::post("P_SCHOOL_CD")) : ""),                      //小学校
                             "J_SCHOOL_CD"                      =>  (VARS::post("J_SCHOOL_CD") ? sprintf("%0".($this->finschoolcdKeta)."s", VARS::post("J_SCHOOL_CD")) : ""),                      //中学校
                             "EVACUATION_AREA"                  =>  VARS::post("EVACUATION_AREA"),                  //避難場所
                             "IMPORTANT_NOTICE"                 =>  VARS::post("IMPORTANT_NOTICE"),                 //留意事項
                             "REMARK"                           =>  VARS::post("REMARK"),                           //備考
                             
                             "BACKUP_DATE"                      =>  VARS::post("BACKUP_DATE"),      //履歴保存用の日付
                             
                             //以下生育歴/訓練機関(arrayのkeyが重複するので、下の処理で設定)
                             
                             //以下、視力・聴力
                             "DATE"                             =>  VARS::post("DATE"),
                             "R_BAREVISION"                     =>  VARS::post("R_BAREVISION"),
                             "R_VISION"                         =>  VARS::post("R_VISION"),
                             "R_VISION_CANTMEASURE"             =>  VARS::post("R_VISION_CANTMEASURE"),
                             "L_BAREVISION"                     =>  VARS::post("L_BAREVISION"),
                             "L_VISION"                         =>  VARS::post("L_VISION"),
                             "L_VISION_CANTMEASURE"             =>  VARS::post("L_VISION_CANTMEASURE"),
                             "RL_BAREVISION"                    =>  VARS::post("RL_BAREVISION"),            //裸眼（両眼）
                             "RL_VISION"                        =>  VARS::post("RL_VISION"),                //強制（両眼）
                             "RL_VISION_CANTMEASURE"            =>  VARS::post("RL_VISION_CANTMEASURE"),    //測定困難（両眼）
                             "R_EAR_DB"                         =>  VARS::post("R_EAR_DB"),
                             "R_EAR"                            =>  VARS::post("R_EAR"),
                             "R_EAR_CANTMEASURE"                =>  VARS::post("R_EAR_CANTMEASURE"),
                             "L_EAR_DB"                         =>  VARS::post("L_EAR_DB"),
                             "L_EAR"                            =>  VARS::post("L_EAR"),
                             "L_EAR_CANTMEASURE"                =>  VARS::post("L_EAR_CANTMEASURE"),
                             "DET_REMARK3"                      =>  VARS::post("DET_REMARK3"),      //両耳
                             "DET_REMARK4"                      =>  VARS::post("DET_REMARK4"),      //閾値（補聴器装用時）
                             "DET_REMARK5"                      =>  VARS::post("DET_REMARK5"),      //閾値（人口内耳）
                             "DET_REMARK6"                      =>  VARS::post("DET_REMARK6"),      //メーカー・機種・シリアル番号
                             "DET_REMARK7"                      =>  VARS::post("DET_REMARK7"),      //主なコミュニケーション

                             //以下、療育・教育歴
                             "RECORD_DIV"                       =>  VARS::post("RECORD_DIV"),       //学校種別他　※フィールド名共通
                             "RECORD_NO"                        =>  VARS::post("RECORD_NO"),        //小、中学校フォーマット切替　※フィールド名共通
                             "SCHOOL_NAME"                      =>  VARS::post("SCHOOL_NAME"),      //学校名
                             "P_J_SCHOOL_CD"                    =>  (VARS::post("P_J_SCHOOL_CD") ? sprintf("%0".($this->finschoolcdKeta)."s", VARS::post("P_J_SCHOOL_CD")) : ""),    //小中学校コード
                             "S_YEAR_MONTH"                     =>  VARS::post("S_YEAR_MONTH"),     //開始年月
                             "E_YEAR_MONTH"                     =>  VARS::post("E_YEAR_MONTH"),     //終了年月
                             "MAJOR_NAME"                       =>  VARS::post("MAJOR_NAME"),       //学部名
                             "GRADE"                            =>  VARS::post("GRADE"),            //学年
                             "HR_CLASS_NAME"                    =>  VARS::post("HR_CLASS_NAME"),    //組名
                             "CLASS_SHUBETSU"                   =>  VARS::post("CLASS_SHUBETSU"),   //学級種別
                             "REMARK"                           =>  VARS::post("REMARK"),           //訓練内容、備考
                             
                             //以下、医療
                             "NAMECD"                           =>  VARS::post("NAMECD"),           //診療科
                             "CENTER_NAME"                      =>  VARS::post("CENTER_NAME"),      //医療機関名
                             "DISEASE_NAME"                     =>  VARS::post("DISEASE_NAME"),     //病名
                             "ATTEND_STATUS"                    =>  VARS::post("ATTEND_STATUS"),    //通院の状況、特記事項

                             //以下、健康管理
                             "MEDICINE_NAME"                    =>  VARS::post("MEDICINE_NAME"),           //薬品名
                             "DISEASE_CONDITION_NAME"           =>  VARS::post("DISEASE_CONDITION_NAME"),  //病名・症状
                             "CARE_WAY"                         =>  VARS::post("CARE_WAY"),                //飲ませ方・配慮事項
                             "MEDICAL_NAMECD"                   =>  VARS::post("MEDICAL_NAMECD"),          //医療的ケアの名称
                             "SCHOOL_CARE"                      =>  VARS::post("SCHOOL_CARE"),             //学校
                             "HOUSE_CARE"                       =>  VARS::post("HOUSE_CARE"),              //家庭・病院
                             "CENTER_CARE"                      =>  VARS::post("CENTER_CARE"),             //事業所
                             "ALLERGIA_FOOD_CAT"                =>  VARS::post("ALLERGIA_FOOD_CAT"),       //除去する食品
                             "ALLERGIA_PLAN"                    =>  VARS::post("ALLERGIA_PLAN"),           //取組プラン有無
                             "ALLERGIA_SPECIAL_REPORT"          =>  VARS::post("ALLERGIA_SPECIAL_REPORT"), //特記事項
                             "ALLERGIA_FOOD_STYLE"              =>  VARS::post("ALLERGIA_FOOD_STYLE"),     //食形態
                             "ALLERGIA_REMARK"                  =>  VARS::post("ALLERGIA_REMARK"),         //その他のアレルギー

                             //以下、福祉
                             "SERVICE_NAME"                     =>  VARS::post("SERVICE_NAME"),         //サービス
                             "EQUIP2_SUPPLY_DATE"               =>  str_replace('/', '-', (VARS::post("EQUIP2_SUPPLY_DATE") ? VARS::post("EQUIP2_SUPPLY_DATE")."-01": VARS::post("SUPPLY_DATE"))), //申請年月(Y/M)
                             "EQUIP2_SERVICE_CENTER_TEXT"       =>  VARS::post("EQUIP2_SERVICE_CENTER_TEXT"),   //製薬会社
                             "EQUIP3_WELFARE_REMARK"            =>  VARS::post("EQUIP3_WELFARE_REMARK"),        //補装具の給付（日常生活用具）備考
                             "ADVICE_SERVICE_CENTER_TEXT"       =>  VARS::post("ADVICE_SERVICE_CENTER_TEXT"),   //相談支援事業所
                             "ADVICE_SERVICE_CHARGE"            =>  VARS::post("ADVICE_SERVICE_CHARGE"),        //担当者
                             "WELFARE_SERVICE_CENTER_TEXT"      =>  VARS::post("WELFARE_SERVICE_CENTER_TEXT"),  //福祉サービス施設
                             "WELFARE_ITEMCD"                   =>  VARS::post("WELFARE_ITEMCD"),               //支給量 単位
                             "WELFARE_ITEMCD2"                  =>  VARS::post("WELFARE_ITEMCD2"),              //支給量 数値
                             "WELFARE_WELFARE_REMARK"           =>  VARS::post("WELFARE_WELFARE_REMARK"),       //頻度

                             //以下、労働
                             "WORK_REMARK"                      =>  VARS::post("WORK_REMARK")           //担当者・内容
                            );

        if (strstr($this->cmd, "growth")) {
            $this->field["REMARK6"] = VARS::post("REMARK6");
        }
        if (strstr($this->cmd, "training")) {
            $this->field["REMARK1"] = VARS::post("REMARK1");
        }
        if (strstr($this->cmd, "healthcare")) {
            if ($this->cmd == "healthcare1_insert" || $this->cmd == "healthcare1_insert_care" || $this->cmd == "healthcare1_insert_spasm"
            || $this->cmd == "healthcare1_update_consid" || $this->cmd == "healthcare1_update_allergen") {
                $this->getRecordDiv = $this->field["RECORD_DIV"];
                $this->getRecordNo = $this->field["RECORD_NO"];
            }

            $this->field["REMARK1"] = VARS::post("REMARK1");
            $this->field["REMARK2"] = VARS::post("REMARK2");
            $this->field["REMARK3"] = VARS::post("REMARK3");
            $this->field["REMARK4"] = VARS::post("REMARK4");
        }
        if (strstr($this->cmd, "welfare")) {
            if ($this->cmd == "welfare1_insert") {
                $this->getRecordDiv = $this->field["RECORD_DIV"];
                $this->getRecordNo = $this->field["RECORD_NO"];
            }
            if ($this->getRecordDiv == '1' && $this->getRecordNo == '2') {
                //補装具等の給付（補装具）
                $this->field["SUPPLY_DATE"] = str_replace('/', '-', (VARS::post("EQUIP2_SUPPLY_DATE") ? VARS::post("EQUIP2_SUPPLY_DATE")."-01": VARS::post("EQUIP2_SUPPLY_DATE")));
                // $this->field["SUPPLY_DATE"] = VARS::post("EQUIP2_SUPPLY_DATE");
                $this->field["SERVICE_CENTER_TEXT"] = VARS::post("EQUIP2_SERVICE_CENTER_TEXT");
            } elseif ($this->getRecordDiv == '1' && $this->getRecordNo == '3') {
                //補装具等の給付（日常生活用具）
                $this->field["WELFARE_REMARK"] = VARS::post("EQUIP3_WELFARE_REMARK");
            } elseif ($this->getRecordDiv == '2') {
                //相談支援事業所
                $this->field["SERVICE_CENTER_TEXT"] = VARS::post("ADVICE_SERVICE_CENTER_TEXT");
                $this->field["SERVICE_CHARGE"] = VARS::post("ADVICE_SERVICE_CHARGE");
            } elseif ($this->getRecordDiv == '3') {
                //事業所サービスの利用
                $this->field["SERVICE_CENTER_TEXT"] = VARS::post("WELFARE_SERVICE_CENTER_TEXT");
                $this->field["ITEMCD"] = VARS::post("WELFARE_ITEMCD");
                $this->field["ITEMCD2"] = VARS::post("WELFARE_ITEMCD2");
                $this->field["WELFARE_REMARK"] = VARS::post("WELFARE_WELFARE_REMARK");
            }
        }
        //以下、福祉の補装具・日常生活用具・サービス内容
        $db = Query::dbCheckOut();
        $this->itemdiv = array();
        for ($flg = 1; $flg <= 3; $flg++) {
            $query = knje390mQuery::getItemdivComboName("{$flg}");
            $result = $db->query($query);
            while ($rowItem = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $cnt = ($flg == "1") ? 6 : 3;
                for ($i = 1; $i <= $cnt; $i++) {
                    $fieldName = "ITEMCD".$i."_".$rowItem["VALUE"];
                    if ($flg == 1) {
                        $name = "EQUIP2_ITEMCD".$i."_".$rowItem["VALUE"];
                    } elseif ($flg == 2) {
                        $name = "EQUIP3_ITEMCD".$i."_".$rowItem["VALUE"];
                    } else {
                        $name = "WELFARE_ITEMCD".$i."_".$rowItem["VALUE"];
                    }
                    $this->field[$name] =  VARS::post($name);
                    $this->field[$fieldName] =  VARS::post($name);
                }

                $this->itemdiv[$flg][] = $rowItem["VALUE"];
            }
            $result->free();
        }
        Query::dbCheckIn($db);

        /***************A アセスメント表 ***************/
        $this->field2 = array("RECORD_DATE"             =>  VARS::post("RECORD_DATE"),                  //作成年月日
                             "WRITING_DATE"             =>  VARS::post("WRITING_DATE"),                 //作成年月日
                             "RECORD_HISTORY"           =>  VARS::post("RECORD_HISTORY"),               //作成履歴
                             "CHALLENGED_NAMES"         =>  VARS::post("CHALLENGED_NAMES"),             //障害名･診断名
                             "CHALLENGED_STATUS"        =>  VARS::post("CHALLENGED_STATUS"),            //障害の実態･特性
                             "MAX_DATA_DIV"             =>  VARS::post("MAX_DATA_DIV"),                 //項目数
                             "RECORD_STAFFNAME"         =>  VARS::post("RECORD_STAFFNAME"),             //作成者

                             "TORIKOMI_B_DATE"          =>  VARS::post("TORIKOMI_B_DATE"),              //B取込対象の日付
                              
                             //以下、発達検査
                             "CHECK_DATE"               =>  VARS::post("CHECK_DATE"),                   //検査日
                             "CHECK_CENTER_TEXT"        =>  VARS::post("CHECK_CENTER_TEXT"),            //検査機関
                             "CENTERCD"                 =>  VARS::post("CENTERCD"),                     //検査機関コード
                             "CHECK_NAME"               =>  VARS::post("CHECK_NAME"),                   //検査名
                             "YOMIKOMI_YEAR"            =>  VARS::post("YOMIKOMI_YEAR")                 //読込年度

                             //以下、教科等の実態
                            , "SUBCLASS"                =>  VARS::post("SUBCLASS")                      //科目コード
                            , "SUBCLASS_STATUS"         =>  VARS::post("SUBCLASS_STATUS")               //実態
                            , "SUBCLASS_FUTURE_CARE"    =>  VARS::post("SUBCLASS_FUTURE_CARE")          //支援
                            , "SELECT_SUBCLASS"         =>  VARS::post("SELECT_SUBCLASS")               //データ存在フラグ
                        );

        if (VARS::post("MAX_DATA_DIV") > 0) {
            $this->subFrm2_maxItemCnt = VARS::post("MAX_DATA_DIV");
            for ($i = 1; $i <= VARS::post("MAX_DATA_DIV"); $i++) {
                $this->field2["DIV".$i."_STATUS"]   = VARS::post("DIV".$i."_STATUS");           //所見（左）
                $this->field2["DIV".$i."_GROWUP"]   = VARS::post("DIV".$i."_GROWUP");           //所見（右）
            }
        }

        /***************支援計画 ***************/
        $this->field3 = array("RECORD_DATE"             =>  VARS::post("RECORD_DATE"),                  //作成年月日
                             "WRITING_DATE"             =>  VARS::post("WRITING_DATE"),                 //作成年月日
                             "RECORD_HISTORY"           =>  VARS::post("RECORD_HISTORY"),               //作成履歴
                             "RECORD_STAFFNAME"         =>  VARS::post("RECORD_STAFFNAME"),             //作成者
                             "CHALLENGED_NAMES"         =>  VARS::post("CHALLENGED_NAMES"),             //障害名等
                             "REASONABLE_ACCOMMODATION" =>  VARS::post("REASONABLE_ACCOMMODATION"),     //合理的配慮
                             "MAX_DATA_DIV_HOPE"        =>  VARS::post("MAX_DATA_DIV_HOPE"),            //項目数 希望
                             "MAX_DATA_DIV_GOALS"       =>  VARS::post("MAX_DATA_DIV_GOALS"),           //項目数 目標

                             "ONES_HOPE_PRESENT"        =>  VARS::post("ONES_HOPE_PRESENT"),            //具体的な支援、連携の記録 学校生活への期待や願い（本人）
                             "GUARDIAN_HOPE_PRESENT"    =>  VARS::post("GUARDIAN_HOPE_PRESENT"),        //具体的な支援、連携の記録 学校生活への期待や願い（保護者）
                             "SUPPORT_GOAL"             =>  VARS::post("SUPPORT_GOAL"),                 //具体的な支援、連携の記録 支援目標
                             "RECORD"                   =>  VARS::post("RECORD"),                       //具体的な支援、連携の記録 評価・連携の記録
                             "MAX_DATA_DIV"             =>  VARS::post("MAX_DATA_DIV"),                 //具体的な支援、連携の記録 項目数
                            );

        if (VARS::post("MAX_DATA_DIV_HOPE") > 0) {
            for ($i = 1; $i <= VARS::post("MAX_DATA_DIV_HOPE"); $i++) {
                $this->field3["DIV".$i."_HOPE1"]  = VARS::post("DIV".$i."_HOPE1");           //所見（左）
                $this->field3["DIV".$i."_HOPE2"]  = VARS::post("DIV".$i."_HOPE2");           //所見（右）
            }
        }
        
        if (VARS::post("MAX_DATA_DIV_GOALS") > 0) {
            for ($i = 1; $i <= VARS::post("MAX_DATA_DIV_GOALS"); $i++) {
                $this->field3["DIV".$i."_GOALS"]   = VARS::post("DIV".$i."_GOALS");           //所見
            }
        }

        if (VARS::post("MAX_DATA_DIV") > 0) {
            for ($i = 1; $i <= VARS::post("MAX_DATA_DIV"); $i++) {
                $this->field3["DIV".$i."_STATUS"]   = VARS::post("DIV".$i."_STATUS");           //所見（左）
                $this->field3["DIV".$i."_STATUS2"]  = VARS::post("DIV".$i."_STATUS2");          //所見（中）
                $this->field3["DIV".$i."_STATUS3"]  = VARS::post("DIV".$i."_STATUS3");          //所見（右）
            }
        }

        /***************移行支援計画 ***************/
        $this->field4 = array("RECORD_DATE"                     =>  VARS::post("RECORD_DATE"),                      //作成年月日
                             "WRITING_DATE"                     =>  VARS::post("WRITING_DATE"),                     //作成年月日
                             "RECORD_HISTORY"                   =>  VARS::post("RECORD_HISTORY"),                   //作成履歴
                             "RECORD_STAFFNAME"                 =>  VARS::post("RECORD_STAFFNAME"),                 //作成者
                             "CHALLENGED_NAMES"                 =>  VARS::post("CHALLENGED_NAMES"),                 //障害名･診断名
                             "CHALLENGED_STATUS"                =>  VARS::post("CHALLENGED_STATUS"),                //障害の実態･特性

                             "LIFESTYLE_STATUS"                 =>  VARS::post("LIFESTYLE_STATUS"),                 //基本的生活習慣身辺自立の実態
                             "SOCIALITY_STATUS"                 =>  VARS::post("SOCIALITY_STATUS"),                 //行動・社会性の実態
                             "COMMUNICATION_STATUS"             =>  VARS::post("COMMUNICATION_STATUS"),             //言語コミュニケーションの実態
                             "PHYSICAL_ACTIVITY_STATUS"         =>  VARS::post("PHYSICAL_ACTIVITY_STATUS"),         //身体・運動の実態
                             "STUDY_STATUS"                     =>  VARS::post("STUDY_STATUS"),                     //学習の実態
                             "INTERESTING_STATUS"               =>  VARS::post("INTERESTING_STATUS"),               //興味・強みの実態
                             
                             "ONES_HOPE_PRESENT"                =>  VARS::post("ONES_HOPE_PRESENT"),                //本人　現在　希望・願い
                             "ONES_HOPE_CAREER"                 =>  VARS::post("ONES_HOPE_CAREER"),                 //本人　進路　希望・願い
                             "ONES_HOPE_AFTER_GRADUATION"       =>  VARS::post("ONES_HOPE_AFTER_GRADUATION"),       //本人　卒業後　希望・願い
                             "GUARDIAN_HOPE_PRESENT"            =>  VARS::post("GUARDIAN_HOPE_PRESENT"),            //保護者　現在　希望・願い
                             "GUARDIAN_HOPE_CAREER"             =>  VARS::post("GUARDIAN_HOPE_CAREER"),             //保護者　進路　希望・願い
                             "GUARDIAN_HOPE_AFTER_GRADUATION"   =>  VARS::post("GUARDIAN_HOPE_AFTER_GRADUATION"),   //保護者　卒業後　希望・願い
                             "TAKEOVER"                         =>  VARS::post("TAKEOVER"),                         //引継ぎ事項
                             
                             "BACKUP_DATE"                      =>  VARS::post("BACKUP_DATE"),                      //履歴保存用の日付
                             "TORIKOMI_B_DATE"                  =>  VARS::post("TORIKOMI_B_DATE"),                  //B取込対象の日付
                             
                             //以下、発達検査
                             "CHECK_DATE"                       =>  VARS::post("CHECK_DATE"),                       //検査日
                             "CHECK_REMARK"                     =>  VARS::post("CHECK_REMARK"),                     //検査結果・所見等
                             
                             //以下、スムーズな以降に向けての支援（テーブルはメイン）
                             "DATA_DIV"                         =>  VARS::request("DATA_DIV"),                      //コンボ切換用
                             //医療
                             "MEDICAL_GOAL_AFTER_GRADUATION"    =>  VARS::post("MEDICAL_GOAL_AFTER_GRADUATION"), //卒業後の姿・目標
                             "MEDICAL_STATUS"                   =>  VARS::post("MEDICAL_STATUS"),                //実態・課題
                             "MEDICAL_SHORT_TERM_GOAL"          =>  VARS::post("MEDICAL_SHORT_TERM_GOAL"),       //短期目標
                             "MEDICAL_TANGIBLE_SUPPORT"         =>  VARS::post("MEDICAL_TANGIBLE_SUPPORT"),      //具体的な支援・手立て
                             //福祉
                             "WELFARE_GOAL_AFTER_GRADUATION"    =>  VARS::post("WELFARE_GOAL_AFTER_GRADUATION"), //卒業後の姿・目標
                             "WELFARE_STATUS"                   =>  VARS::post("WELFARE_STATUS"),                //実態・課題
                             "WELFARE_SHORT_TERM_GOAL"          =>  VARS::post("WELFARE_SHORT_TERM_GOAL"),       //短期目標
                             "WELFARE_TANGIBLE_SUPPORT"         =>  VARS::post("WELFARE_TANGIBLE_SUPPORT"),      //具体的な支援・手立て
                             //労働
                             "WORK_GOAL_AFTER_GRADUATION"       =>  VARS::post("WORK_GOAL_AFTER_GRADUATION"),    //卒業後の姿・目標
                             "WORK_STATUS"                      =>  VARS::post("WORK_STATUS"),                   //実態・課題
                             "WORK_SHORT_TERM_GOAL"             =>  VARS::post("WORK_SHORT_TERM_GOAL"),          //短期目標
                             "WORK_TANGIBLE_SUPPORT"            =>  VARS::post("WORK_TANGIBLE_SUPPORT"),         //具体的な支援・手立て
                             //家庭地域生活
                             "COMMU_GOAL_AFTER_GRADUATION"      =>  VARS::post("COMMU_GOAL_AFTER_GRADUATION"),   //卒業後の姿・目標
                             "COMMU_STATUS"                     =>  VARS::post("COMMU_STATUS"),                  //実態・課題
                             "COMMU_SHORT_TERM_GOAL"            =>  VARS::post("COMMU_SHORT_TERM_GOAL"),         //短期目標
                             "COMMU_TANGIBLE_SUPPORT"           =>  VARS::post("COMMU_TANGIBLE_SUPPORT"),        //具体的な支援・手立て
                             //相談の記録(別テーブル)
                             "MEETING_NAME"                     =>  VARS::post("MEETING_NAME"),                  //会議名
                             "MEETING_DATE"                     =>  VARS::post("MEETING_DATE"),                  //会議日
                             "TEAM_MEMBERS"                     =>  VARS::post("TEAM_MEMBERS"),                  //構成員
                             "MEETING_SUMMARY"                  =>  VARS::post("MEETING_SUMMARY"),               //会議概要

                             //以下、進路指導計画
                             "RECORD_DIV"                   =>  VARS::request("RECORD_DIV"),                 //コンボ切換用　※共通
                             "RECORD_NO"                    =>  VARS::request("RECORD_NO"),                  //コンボ切換用　※共通
                             "MEETING_NAME"                 =>  VARS::post("MEETING_NAME"),                  //会議名
                             "MEETING_DATE"                 =>  VARS::post("MEETING_DATE"),                  //会議日
                             "TEAM_MEMBERS"                 =>  VARS::post("TEAM_MEMBERS"),                  //構成員
                             "MEETING_SUMMARY"              =>  VARS::post("MEETING_SUMMARY"),               //概要
                             "WORK_TRAINING_PLACE"          =>  VARS::post("WORK_TRAINING_PLACE"),           //実習、見学場所
                             "WORK_TRAINING_S_DATE"         =>  VARS::post("WORK_TRAINING_S_DATE"),          //実習、見学期日
                             "WORK_TRAINING_E_DATE"         =>  VARS::post("WORK_TRAINING_E_DATE"),          //実習、見学期日終了日
                             "WORK_TRAINING_CONTENTS"       =>  VARS::post("WORK_TRAINING_CONTENTS"),        //実習、見学内容
                             "WORK_TRAINING_GOAL"           =>  VARS::post("WORK_TRAINING_GOAL"),            //実習、見学目標
                             "WORK_TRAINING_SUPPORT"        =>  VARS::post("WORK_TRAINING_SUPPORT"),         //実習、見学支援、手立て
                             "WORK_TRAINING_SUPPORT_TARGET" =>  VARS::post("WORK_TRAINING_SUPPORT_TARGET"),  //実習、見学支援、手立ての対象
                             "WORK_TRAINING_RESULT"         =>  VARS::post("WORK_TRAINING_RESULT"),          //実習、見学成果
                             "WORK_TRAINING_CHALLENGE"      =>  VARS::post("WORK_TRAINING_CHALLENGE"),       //実習、見学課題
                             "CAREER_GUIDANCE_RESULT"       =>  VARS::post("CAREER_GUIDANCE_RESULT"),        //年間の成果
                             "CAREER_GUIDANCE_CHALLENGE"    =>  VARS::post("CAREER_GUIDANCE_CHALLENGE"),     //年間の課題
                             "DETERMINED_COURSE"            =>  VARS::post("DETERMINED_COURSE"),             //事業所、福祉利用
                             "COURSE_CONTENTS"              =>  VARS::post("COURSE_CONTENTS"),               //内容
                             "REMARK"                       =>  VARS::post("REMARK")                         //その他
                            );

        /***************サポートブック ***************/
        $this->field5 = array("RECORD_DATE"                 =>  VARS::post("RECORD_DATE"),                  //作成年月日
                             "WRITING_DATE"                 =>  VARS::post("WRITING_DATE"),                  //作成年月日
                             "RECORD_HISTORY"               =>  VARS::post("RECORD_HISTORY"),                //作成履歴
                             "CHALLENGED_NAMES"             =>  VARS::post("CHALLENGED_NAMES"),              //障害名
                             "CHALLENGED_POPULAR_NAME"      =>  VARS::post("CHALLENGED_POPULAR_NAME"),       //呼び名
                             "CHRONIC_DISEASE"              =>  VARS::post("CHRONIC_DISEASE"),               //持病名
                             "HOW_COMMUNICATION"            =>  VARS::post("HOW_COMMUNICATION"),             //コミュニケーション(会話)の仕方
                             "TROUBLED_BEHAVIOR_SUPPORT"    =>  VARS::post("TROUBLED_BEHAVIOR_SUPPORT"),     //困った行動の対処法
                             "BAD_CONDITION_SIGN"           =>  VARS::post("BAD_CONDITION_SIGN")             //不調のサイン
                            );
                            
        /***************引継資料(担任) ***************/
        $this->field6 = array("RECORD_DATE"                 =>  VARS::post("RECORD_DATE"),                  //作成年月日
                             "WRITING_DATE"                 =>  VARS::post("WRITING_DATE"),                 //作成年月日
                             "RECORD_HISTORY"               =>  VARS::post("RECORD_HISTORY"),                //作成履歴
                             "DATA_DIV"                     =>  VARS::post("DATA_DIV"),                     //種別
                             "CAN_BE_NO_SUPPORT"            =>  VARS::post("CAN_BE_NO_SUPPORT"),            //支援なしでできること
                             "CAN_BE_SOME_SUPPORT"          =>  VARS::post("CAN_BE_SOME_SUPPORT"),          //一部支援でできること
                             "MEANS"                        =>  VARS::post("MEANS"),                        //手立て
                             "SHORT_TERM_GOAL"              =>  VARS::post("SHORT_TERM_GOAL"),              //短期目標
                             "GOAL_FUTURE"                  =>  VARS::post("GOAL_FUTURE")                   //将来に目指す
                            );
        /***************引継資料(事業者) ***************/
        $this->field7 = array("RECORD_DATE"                 =>  VARS::post("RECORD_DATE"),                  //作成年月日
                             "WRITING_DATE"                =>  VARS::post("WRITING_DATE"),                 //作成年月日
                             "RECORD_HISTORY"               =>  VARS::post("RECORD_HISTORY"),               //作成履歴
                             "DATA_DIV"                     =>  VARS::post("DATA_DIV"),                     //種別
                             "OUTLINE"                      =>  VARS::post("OUTLINE"),                      //概要
                             "MEASURE"                      =>  VARS::post("MEASURE"),                      //方策
                             "FUTURE_DEVELOPMENT"           =>  VARS::post("FUTURE_DEVELOPMENT"),           //今後の展開
                             "REMARK"                       =>  VARS::post("REMARK")                        //備考
                            );

        /***************関係者間資料 ***************/
        $this->field8 = array("RECORD_DATE"                 =>  VARS::post("RECORD_DATE"),                  //作成年月日
                             "WRITING_DATE"                 =>  VARS::post("WRITING_DATE"),                     //作成年月日
                             "RECORD_HISTORY"               =>  VARS::post("RECORD_HISTORY"),                   //作成履歴
                             "WRITER"                       =>  VARS::post("WRITER"),                           //記入者
                             "MEETING_NAME"                 =>  VARS::post("MEETING_NAME"),                     //会議名
                             "MEETING_DATE"                 =>  VARS::post("MEETING_DATE"),                     //打合せ日
                             "MEETING_SHOUR"                =>  VARS::post("MEETING_SHOUR"),                    //打合せ開始時間(時)
                             "MEETING_SMINUTES"             =>  VARS::post("MEETING_SMINUTES"),                    //打合せ開始時間(分)
                             "MEETING_EHOUR"                =>  VARS::post("MEETING_EHOUR"),                    //打合せ終了時間(時)
                             "MEETING_EMINUTES"             =>  VARS::post("MEETING_EMINUTES"),                    //打合せ終了時間(分)
                             "MEETING_PALCE"                =>  VARS::post("MEETING_PALCE"),                    //打合せ場所
                             "MEETING_PARTICIPANT"          =>  VARS::post("MEETING_PARTICIPANT"),              //参加者
                             
                             "PROBLEM_EVENT_DOCUMENTS"        =>  VARS::post("PROBLEM_EVENT_DOCUMENTS"),         //問題事象
                             "PROBLEM_ANALYSIS_DOCUMENTS"     =>  VARS::post("PROBLEM_ANALYSIS_DOCUMENTS"),      //事象分析
                             "PROBLEM_DEAL_DOCUMENTS"         =>  VARS::post("PROBLEM_DEAL_DOCUMENTS"),          //課題対応
                             "STAFF_ROLE_DOCUMENTS"           =>  VARS::post("STAFF_ROLE_DOCUMENTS"),            //指導者の役割等
                             "GUARDIAN_COOPERATION_DOCUMENTS" =>  VARS::post("GUARDIAN_COOPERATION_DOCUMENTS"),  //保護者連携
                             "WELFARE_COOPERATION_DOCUMENTS"  =>  VARS::post("WELFARE_COOPERATION_DOCUMENTS"),   //福祉連携
                             "OTHERS_REMARK_DOCUMENTS"        =>  VARS::post("OTHERS_REMARK_DOCUMENTS")          //その他
                            );
                            
        /***************実態表***************/
        $this->field9 = array("RECORD_DATE"                 =>  VARS::post("RECORD_DATE"),                      //作成年月日
                             "RECORD_HISTORY"               =>  VARS::post("RECORD_HISTORY"),                   //履歴
                             "WRITING_DATE"                 =>  VARS::post("WRITING_DATE"),                     //作成年月日
                             "RECORD_STAFFNAME"             =>  VARS::post("RECORD_STAFFNAME"),                 //記入者
                             "CHALLENGED_NAMES"             =>  VARS::post("CHALLENGED_NAMES"),                 //会議名
                             "TORIKOMI_B_DATE"              =>  VARS::post("TORIKOMI_B_DATE"),
                             //以下、実習の記録
                             "COMPANY_NAME"                 =>  VARS::post("COMPANY_NAME"),   //実習先
                             "START_DATE"                   =>  str_replace("/", "-", VARS::post("START_DATE")),     //開始日付
                             "FINISH_DATE"                  =>  VARS::post("FINISH_DATE"),    //終了日付
                             "REMARK"                       =>  VARS::post("REMARK")         //備考
                            );
        if (VARS::post("MAX_DATA_DIV") > 0) {
            for ($i = 1; $i <= VARS::post("MAX_DATA_DIV"); $i++) {
                $this->field9["DIV".$i."_STATUS"]   = VARS::post("DIV".$i."_STATUS");           //所見（左）
                $this->field9["DIV".$i."_GROWUP"]   = VARS::post("DIV".$i."_GROWUP");           //所見（右）
            }
        }

        $this->checked = VARS::request("CHECKED");     //チェック

        //アセスメント表取込時の親画面コード
        $this->parantGamen      = VARS::get("ASSESS_TORIKOMI_PARENT");
        $this->torikomiTarget   = VARS::get("ASSESS_TORIKOMI_TARGET");

        if (VARS::get("SCHREGNO") != "") {
            $this->schregno = VARS::get("SCHREGNO");
            unset($this->seq);
            unset($this->relano);
        }
        if (VARS::get("NAME") != "") {
            $this->name = VARS::get("NAME");
        }
        if (VARS::get("GRADE") != "") {
            $this->grade = VARS::get("GRADE");
        }
        if (VARS::get("HR_CLASS") != "") {
            $this->hr_class = VARS::get("HR_CLASS");
        }
        if (VARS::get("ATTENDNO") != "") {
            $this->attendno = VARS::get("ATTENDNO");
        }
        if (VARS::get("TYPE") != "") {
            $this->type = VARS::get("TYPE");
        }
        if (VARS::get("mode") != "") {
            $this->mode = VARS::get("mode");
        }
        if (VARS::get("MAIN_YEAR") != "") {
            $this->main_year = VARS::get("MAIN_YEAR");
        }
        if (VARS::get("RECORD_DATE") != "") {
            $this->record_date = VARS::get("RECORD_DATE");
            $this->record_date = str_replace("/", "-", $this->record_date);
        }
        if ($this->record_date != "") {
            $recordDateArr = split("-", str_replace("/", "-", $this->record_date));
            $this->main_year = $recordDateArr[0];
        }

        //履歴用GETパラメータ
        if (VARS::get("YEAR") != "") {
            $this->getYear = VARS::get("YEAR");
        }
        if (VARS::get("RECORD_DIV") != "") {
            $this->getRecordDiv = VARS::get("RECORD_DIV");
        }
        if (VARS::get("RECORD_NO") != "") {
            $this->getRecordNo = VARS::get("RECORD_NO");
        }
        if (VARS::get("RECORD_SEQ") != "") {
            $this->getRecordSeq = VARS::get("RECORD_SEQ");
        }
        if (VARS::get("RELANO") != "") {
            $this->getRelano = VARS::get("RELANO");
        }
        if (VARS::get("SUBCLASS") != "") {
            $this->getSubclass = VARS::get("SUBCLASS");
        }
        if (VARS::get("START_DATE") != "") {
            $this->startDate = VARS::get("START_DATE");
        }

        //リストの年組コンボが変更されたとき
        if (VARS::get("init")) {
            unset($this->schregno);
            unset($this->name);
            unset($this->grade);
            unset($this->hr_class);
            unset($this->attendno);
            unset($this->mode);
            
            unset($this->main_year);
            unset($this->record_date);

            unset($this->getYear);
            unset($this->getRecordDiv);
            unset($this->getRecordNo);
            unset($this->getRecordSeq);
            unset($this->getRelano);
            unset($this->type);
        }

        if ($this->cmd == "edit") {
            unset($this->getYear);
            unset($this->getRecordDiv);
            unset($this->getRecordNo);
            unset($this->getRecordSeq);
            unset($this->getRelano);
        }
    }

    //年度データ新規作成用を返します
    public function &getUpdateNewModel()
    {
        $db = Query::dbCheckOut();

        $cmd = preg_split("{_}", $this->cmd);
        //フィールド番号設定 (B:1, C:3, D:4, A:2)
        $fieldNo = substr($cmd[0], -1);//A:2, C:3, E:5, F:6, G:7, H:8のみ
        
        $dataCount = knje390mQuery::getUpdateNewData($db, $this, $cmd[0]);
        
        //更新履歴作成
        knje390mQuery::getUpdateLog($db, $this, "", $fieldNo, $cmd[1], $dataCount);
    
        $this->cmd = $cmd[0];
        $this->setMessage("MSG201");
        Query::dbCheckIn($db);

        return;
    }

    //履歴作成用のモデルを返します
    public function &getUpdateRirekiModel()
    {
        $db = Query::dbCheckOut();

        $cmd = preg_split("{_}", $this->cmd);
        //フィールド番号設定 (B:1, C:3, D:4, A:2)
        $fieldNo = substr($cmd[0], -1);
        if ($fieldNo === '1') {
            $setfield = $this->field;
        } elseif ($fieldNo === '2') {
            $setfield = $this->field2;
        } elseif ($fieldNo === '3') {
            $setfield = $this->field3;
        } else {
            $setfield = $this->field4;
        }

        $this->record_date = str_replace("/", "-", $this->record_date);
        //日付チェック
        if ($this->record_date == CTRL_DATE) {
            $this->setWarning("MSG203", "作成日付と元データの日付が同一の場合、処理できません。");
            return false;
        }

        $setBackupDate = str_replace("/", "-", $setfield["BACKUP_DATE"]);
        knje390mQuery::getUpdateRirekiData($db, $this, $setBackupDate, $cmd[0]);
        //更新履歴作成
        knje390mQuery::getUpdateLog($db, $this, "", $fieldNo, $cmd[1], "");

        $this->record_date = CTRL_DATE;
        $this->cmd = $cmd[0];
        $this->setMessage("MSG201");
        Query::dbCheckIn($db);

        return;
    }

    //入力項目チェック
    public function validateInput($db, $setfield, $fielddata, $subfielddata, $cmd0, $cmd1, $cmd2 = "")
    {
        if ($this->schregno == "") {
            $this->setWarning("MSG304");
            return false;
        }

        //基礎情報->健康管理
        if ($cmd0 === 'healthcare1') {
            if ($cmd2 === 'allergen' || $cmd2 === 'consid') {
                //「アレルギー」or「配慮事項」
                $this->getRecordSeq = '1';
            }
        }

        //フィールド名/　$key
        //サイズ/　$val[0]
        //名称/　$val[1]
        //データ形式/　$val[2]
        foreach ($fielddata as $key => $val) {
            /*******履歴画面(UPDATE、DELETE)*******/
            if ($cmd1 === 'update' || $cmd1 === 'delete') {
                if ($cmd0 !== 'family') {
                    if ($this->getRecordSeq == "") {
                        $this->setWarning("MSG308", "111".$this->getRecordSeq."_");
                        return false;
                    }
                } else {
                    if ($this->getRelano == "") {
                        $this->setWarning("MSG308", "222");
                        return false;
                    }
                }
                //文字サイズチェック
                if ($cmd1 === 'update') {
                    if ($val["type"] != "TEXT" || !$val["gyou"] || $val["gyou"] <= 0) {
                        if (strlen($setfield[$key]) > $val["moji"]) {
                            $this->setWarning("MSG915", "(".$val["title"]."は".$val["moji"]."バイトまでです)");
                            return false;
                        }
                    } else {
                        if ($this->validate_row_cnt($setfield[$key], ($val["moji"] * 2)) > $val["gyou"]) {
                            if ($val["gyou"] > 1) {
                                $this->setWarning("MSG915", "({$val["title"]}は全角{$val["moji"]}文字X{$val["gyou"]}行までです)");
                            } else {
                                $this->setWarning("MSG915", "({$val["title"]}は全角{$val["moji"]}文字までです)");
                            }
                            return false;
                        }
                    }
                }

                //RECORD_DIV
                if ($key === 'RECORD_DIV') {
                    if ($this->getRecordDiv == "") {
                        $this->setWarning("MSG308", "333");
                        return false;
                    }
                    if ($setfield[$key]) {
                        if ($key === 'RECORD_DIV') {
                            if ($setfield[$key] != $this->getRecordDiv) {
                                $this->setWarning("MSG308", "444");
                                return false;
                            }
                        }
                    }
                }
                //RECORD_NO
                if ($key === 'RECORD_NO') {
                    if ($this->getRecordNo == "") {
                        $this->setWarning("MSG308", "555");
                        return false;
                    }
                    if ($setfield[$key]) {
                        if ($key === 'RECORD_NO') {
                            if ($setfield[$key] != $this->getRecordNo) {
                                $this->setWarning("MSG308", "666");
                                return false;
                            }
                        }
                    }
                }
                //*******履歴画面(INSERT)*******/
            } elseif ($cmd1 === 'insert') {
                //文字サイズチェック
                if ($val["type"] != "TEXT" || !$val["gyou"] || $val["gyou"] <= 0) {
                    if (strlen($setfield[$key]) > $val["moji"]) {
                        $this->setWarning("MSG915", "(".$val["title"]."は".$val["moji"]."バイトまでです)");
                        return false;
                    }
                } else {
                    if ($this->validate_row_cnt($setfield[$key], ($val["moji"] * 2)) > $val["gyou"]) {
                        if ($val["gyou"] > 1) {
                            $this->setWarning("MSG915", "({$val["title"]}は全角{$val["moji"]}文字X{$val["gyou"]}行までです)");
                        } else {
                            $this->setWarning("MSG915", "({$val["title"]}は全角{$val["moji"]}文字までです)");
                        }
                        return false;
                    }
                }

                //RECORD_DIV
                if ($cmd0 !== 'healthcare1') {
                    if ($key === 'RECORD_DIV') {
                        if (!$setfield[$key]) {
                            $this->setWarning("MSG304", "(種別)");
                            return false;
                        }
                    }
                }
                //RECORD_NO
                if ($cmd0 !== 'educate1' && $cmd0 !== 'welfare1' && $cmd0 !== 'careerguidance4') {
                    if ($key === 'RECORD_NO') {
                        if (!$setfield[$key]) {
                            $this->setWarning("MSG304", "(区分)");
                            return false;
                        }
                    }
                }
                /*******MAIN画面または履歴固定データ(UPDATEのみ)*******/
            } elseif ($cmd1 === 'updatemain' || $cmd1 === 'update2') {
                //作成年月日チェック
                if ($key === 'RECORD_DATE') {
                    if (!$setfield[$key]) {
                        $this->setWarning("MSG301", "(作成年月日)");
                        return false;
                    }
                }
                //文字サイズチェック
                if ($val["type"] != "TEXT" || !$val["gyou"] || $val["gyou"] <= 0) {
                    if (strlen($setfield[$key]) > $val["moji"]) {
                        $this->setWarning("MSG915", "(".$val["title"]."は".$val["moji"]."バイトまでです)");
                        return false;
                    }
                } else {
                    if ($this->validate_row_cnt($setfield[$key], ($val["moji"] * 2)) > $val["gyou"]) {
                        if ($val["gyou"] > 1) {
                            $this->setWarning("MSG915", "({$val["title"]}は全角{$val["moji"]}文字X{$val["gyou"]}行までです)");
                        } else {
                            $this->setWarning("MSG915", "({$val["title"]}は全角{$val["moji"]}文字までです)");
                        }
                        return false;
                    }
                }
            }
        }
        //レコード数チェック(発作の追加登録時のみ)
        if ($cmd0 === 'healthcare1' && $cmd1 === 'insert' && $cmd2 == 'spasm') {
            $chkCnt = $db->getOne(knje390mQuery::getSubQuery1HealthcareRecordCnt($this, "4"));
            //既に3件以上登録されていれば、登録エラーとする
            if ($chkCnt >= 3) {
                $this->setWarning("MSG915", "(発作の登録は3件までです)");
                return false;
            }
        }
        //レコード数チェック(福祉の追加登録時のみ)
        if ($cmd0 === 'welfare1' && $cmd1 === 'insert' && ($this->field["RECORD_DIV"] == "1" || $this->field["RECORD_DIV"] == "3")) {
            $chkCnt = $db->getOne(knje390mQuery::getSubQuery1WelfareGetRecordCnt($this));
            //既に10件以上登録されていれば、登録エラーとする
            if ($chkCnt >= 10) {
                $this->setWarning("MSG915", "(データの登録は10件までです)");
                return false;
            }
        }
        //メイン画面、別テーブル用の文字サイズチェック
        if ($cmd0 === 'subform2' || $cmd0 === 'subform3' || $cmd0 === 'subform3ConcreteSupport' || $cmd0 === 'subform4' || $cmd0 === 'visionEar1'
        || $cmd0 === 'subform6' || $cmd0 === 'subform7' || $cmd0 === 'subform8' || $cmd0 === 'subformZittai') {
            foreach ($subfielddata as $key => $val) {
                if ($val["type"] != "TEXT" || !$val["gyou"] || $val["gyou"] <= 0) {
                    if (strlen($setfield[$key]) > $val["moji"]) {
                        $this->setWarning("MSG915", "(".$val["title"]."は".$val["moji"]."バイトまでです)");
                        return false;
                    }
                } else {
                    if ($this->validate_row_cnt($setfield[$key], ($val["moji"] * 2)) > $val["gyou"]) {
                        if ($val["gyou"] > 1) {
                            $this->setWarning("MSG915", "({$val["title"]}は全角{$val["moji"]}文字X{$val["gyou"]}行までです)");
                        } else {
                            $this->setWarning("MSG915", "({$val["title"]}は全角{$val["moji"]}文字までです)");
                        }
                        return false;
                    }
                }
            }
        }
        return true;
    }

    //更新用のモデルを返します
    public function &getUpdateModel()
    {
        $db = Query::dbCheckOut();
        $cmd = preg_split("{_}", $this->cmd);

        //フィールド変換
        if ($this->field["S_YEAR_MONTH"]) {
            $this->field["S_YEAR_MONTH"] = $this->getSeirekiHenkan($this->field["S_YEAR_MONTH"]);
        }
        if ($this->field["E_YEAR_MONTH"]) {
            $this->field["E_YEAR_MONTH"] = $this->getSeirekiHenkan($this->field["E_YEAR_MONTH"]);
        }
        if ($this->field["CHALLENGED_CARD_CHECK_YM"]) {
            $this->field["CHALLENGED_CARD_CHECK_YM"] = $this->getSeirekiHenkan($this->field["CHALLENGED_CARD_CHECK_YM"]);
        }
        if ($this->field["CHALLENGED_CARD_GRANT_YM"]) {
            $this->field["CHALLENGED_CARD_GRANT_YM"] = $this->getSeirekiHenkan($this->field["CHALLENGED_CARD_GRANT_YM"]);
        }
        if ($this->field["CHALLENGED_CARD_BAST_YM"]) {
            $this->field["CHALLENGED_CARD_BAST_YM"] = $this->getSeirekiHenkan($this->field["CHALLENGED_CARD_BAST_YM"]);
        }

        //$cmd[0] == "check2"の時、この関数から下位の処理でのみ、"CHECK_DATA"をDATE型にする。
        if ($cmd[0] == "check2") {
            $bakChkData = $this->field2["CHECK_DATE"];
            //年月指定を"1日固定の年-月-日"に変換して登録する。
            //文字数チェック等の後で行うとDATE型のチェックができないので、チェック前に設定。
            if ($this->field2["CHECK_DATE"]) {
                $datechgwk = new mycalendar();
                $this->field2["CHECK_DATE"] = $datechgwk->ChgJToW($this->field2["CHECK_DATE"]."/01");
            }
        }

        //テーブル名、更新フィールドデータを取得
        $tablename = "";
        $fielddata = array();
        $setfield = "";
        //テーブル名、フィールドデータ取得
        $tablename = $this->gettablename($cmd[0]);

        $fielddata = $this->getfielddata($cmd[0], $cmd[1], $cmd[2]);
        if ($cmd[0] === 'welfare1' || $cmd[0] === 'visionEar1' || $cmd[0] === 'subform2' || $cmd[0] === 'subform4' || $cmd[0] === 'subform6' || $cmd[0] === 'subform7' || $cmd[0] === 'subform8' || $cmd[0] === 'subformZittai') {
            $subtablename = $this->getsubtablename($cmd[0]);
            $subfielddata = $this->getsubfielddata($db, $cmd[0]);
        }
        //フィールド番号設定 (B:1, C:3, D:4, A:2, サポートブック:5, 引継仕様(担任):6, 引継仕様(事業者):7, 関係者間資料:8)
        $fieldNo = substr($cmd[0], -1);
        if ($fieldNo === '1') {
            $setfield = $this->field;
        } elseif ($fieldNo === '2') {
            $setfield = $this->field2;
        } elseif ($fieldNo === '3') {
            $setfield = $this->field3;
        } elseif ($fieldNo === '4') {
            $setfield = $this->field4;
        } elseif ($fieldNo === '5') {
            $setfield = $this->field5;
        } elseif ($fieldNo === '6') {
            $setfield = $this->field6;
        } elseif ($fieldNo === '7') {
            $setfield = $this->field7;
        } elseif ($fieldNo === '8') {
            $setfield = $this->field8;
        }
        //実態表
        if ($cmd[0] === 'subformZittai') {
            $setfield = $this->field9;
            $fieldNo = '9';
        }
        if ($setfield["RECORD_DATE"]) {
            //データ新規作成時
            $this->record_date = $setfield["RECORD_DATE"];
            $this->record_date = str_replace("/", "-", $this->record_date);
        }
        $setfield["WRITING_DATE"] = $this->record_date;


        if ($cmd[0] === 'growth1') {
            //エラー回避用(更新処理が共通なので、ここで設定)
            $this->getRecordDiv = '001';
            $this->getRecordSeq = 1;
        } elseif ($cmd[0] === 'training1') {
            //エラー回避用(更新処理が共通なので、ここで設定)
            $this->getRecordDiv = '002';
            $this->getRecordSeq = 1;
        } elseif ($cmd[0] === 'visionEar1') {
            //エラー回避用(更新処理が共通なので、ここで設定)
            $this->getRecordDiv = '001';
            $this->getRecordSeq = 1;
        }

        //入力チェック
        if ($this->validateInput($db, $setfield, $fielddata, $subfielddata, $cmd[0], $cmd[1], $cmd[2])) {
            //更新、追加、削除処理
            if ($cmd[1] === 'updatemain') {
                knje390mQuery::getUpdateMainQuery($db, $this, $setfield, $tablename, $fielddata, $fieldNo, $cmd[0], $cmd[1], $cmd[2]);
                if ($cmd[0] === 'subform2' || $cmd[0] === 'subform4' || $cmd[0] === 'subform8' || $cmd[0] === 'subformZittai') {
                    knje390mQuery::getUpdateMain2Query($db, $this, $setfield, $subtablename, $subfielddata, $fieldNo, $cmd[0], $cmd[1], $cmd[2]);
                } elseif ($cmd[0] === 'subform6' || $cmd[0] === 'subform7') {
                    knje390mQuery::getUpdateMain2_2Query($db, $this, $setfield, $subtablename, $subfielddata, $fieldNo, $cmd[0], $cmd[1], $cmd[2]);
                }
            } elseif ($cmd[1] === 'update') {
                if ($cmd[0] === 'visionEar1') {
                    //視力・聴力については、保険系のテーブルに登録するため、処理を分ける
                    knje390mQuery::getUpdateMedexamSubQuery($db, $this, $setfield, $subtablename, $subfielddata);
                    knje390mQuery::getUpdateMedexamQuery($db, $this, $setfield, $tablename, $fielddata);
                    knje390mQuery::getUpdateMedexamHdatQuery($db, $this, $this->field["DATE"]);
                } else {
                    knje390mQuery::getUpdateRecordQuery($db, $this, $setfield, $tablename, $fielddata, $fieldNo, $cmd[0], $cmd[2]);
                    if ($cmd[0] === 'welfare1' && ($this->field["RECORD_DIV"] == "1" || $this->field["RECORD_DIV"] == "3")) {
                        knje390mQuery::getUpdateRecordSubQuery($db, $this, $setfield, $subtablename, $subfielddata, $fieldNo, $cmd[0], $cmd[1], $cmd[2]);
                    }
                }
            } elseif ($cmd[1] === 'update2') {
                knje390mQuery::getUpdateRecord2Query($db, $this, $setfield, $tablename, $fielddata, $fieldNo, $cmd[0], $cmd[1], $cmd[2]);
            } elseif ($cmd[1] === 'insert') {
                //戻り値にフィールド値をセット
                $this->getRecordSeq = knje390mQuery::getInsertRecordQuery($db, $this, $setfield, $tablename, $fielddata, $fieldNo, $cmd[0], $cmd[1], $cmd[2]);
                if ($fieldNo === '1') {
                    $this->getYear = ($this->getYear == "") ? CTRL_YEAR : $this->getYear;
                    $this->getRecordDiv = $this->field["RECORD_DIV"];
                    $this->getRecordNo = $this->field["RECORD_NO"];
                    if ($cmd[0] === 'educate1' || $cmd[0] === 'welfare1') {
                        if ($this->field["RECORD_NO"]) {
                            $this->getRecordNo = $this->field["RECORD_NO"];
                        } else {
                            $this->getRecordNo = 1;
                        }
                        if ($cmd[0] === 'welfare1' && ($this->field["RECORD_DIV"] == "1" || $this->field["RECORD_DIV"] == "3")) {
                            knje390mQuery::getUpdateRecordSubQuery($db, $this, $setfield, $subtablename, $subfielddata, $fieldNo, $cmd[0], $cmd[1], $cmd[2]);
                        }
                    }
                } elseif ($fieldNo === '2') {
                    $this->getYear = ($this->getYear == "") ? CTRL_YEAR : $this->getYear;
                    $this->getRecordDiv = $this->field2["RECORD_DIV"];
                    $this->getRecordNo = $this->field2["RECORD_NO"];
                } elseif ($fieldNo === '3') {
                    $this->getYear = ($this->getYear == "") ? CTRL_YEAR : $this->getYear;
                } else {
                    $this->getYear = ($this->getYear == "") ? CTRL_YEAR : $this->getYear;
                    $this->getRecordDiv = $this->field4["RECORD_DIV"];
                    if ($cmd[0] === 'careerguidance4') {
                        if ($this->field4["RECORD_NO"]) {
                            $this->getRecordNo = $this->field4["RECORD_NO"];
                        } else {
                            $this->getRecordNo = 1;
                        }
                    }
                }
            } elseif ($cmd[1] === 'delete') {
                knje390mQuery::getDeleteRecordQuery($db, $this, $setfield, $tablename, $fielddata, $fieldNo, $cmd[0]);
                //種別、区分以外はUNSET
                if ($fieldNo === '1') {
                    if ($cmd[0] === 'welfare1' && ($this->field["RECORD_DIV"] == "1" || $this->field["RECORD_DIV"] == "3")) {
                        knje390mQuery::getDeleteRecordQuery($db, $this, $setfield, $subtablename, $subfielddata, $fieldNo, $cmd[0]);
                    }
                    unset($this->field);
                    $this->field["RECORD_DIV"] = $this->getRecordDiv;
                    $this->field["RECORD_NO"] = $this->getRecordNo;
                } elseif ($fieldNo === '2') {
                    unset($this->field2);
                    $this->field2["RECORD_DIV"] = $this->getRecordDiv;
                    $this->field2["RECORD_NO"] = $this->getRecordNo;
                } elseif ($fieldNo === '3') {
                    unset($this->field3);
                } else {
                    $set_tmp = "";
                    if ($this->field4["DATA_DIV"]) {
                        $set_tmp = $this->field4["DATA_DIV"];
                    }
                    unset($this->field4);
                    $this->field4["RECORD_DIV"] = $this->getRecordDiv;
                    $this->field4["RECORD_NO"] = $this->getRecordNo;
                    if ($set_tmp) {
                        $this->field4["DATA_DIV"] = $set_tmp;
                    }
                }
            }

            //更新履歴作成
            knje390mQuery::getUpdateLog($db, $this, $tablename, $fieldNo, $cmd[1], "");
            if ($cmd[1] === 'delete') {
                unset($this->getYear);
                unset($this->getRecordDiv);
                unset($this->getRecordNo);
                unset($this->getRecordSeq);
            }

            Query::dbCheckIn($db);

            $this->cmd = $cmd[0]."A";
            $this->setMessage("MSG201");
        }

        //$cmd[0] == "check2"の時、この関数から戻る際、"CHECK_DATA"を元に戻す。
        if ($cmd[0] == "check2") {
            $this->field2["CHECK_DATE"] = $bakChkData;
        }


        return;
    }

    //更新用のモデルを返します(家族構成用)
    public function &getUpdateEtcModel()
    {
        $db = Query::dbCheckOut();

        $cmd = preg_split("{_}", $this->cmd);

        $fielddata = $this->getfielddata($cmd[0], $cmd[1]);
        $setfield = $this->subfield;

        //入力チェック
        if ($this->validateInput($db, $setfield, $fielddata, $subfielddata, $cmd[0], $cmd[1])) {
            if ($cmd[1] === 'update' || $cmd[1] === 'insert') {
                $this->getRelano = knje390mQuery::getInsertUpdateFamilyQuery($db, $this, $setfield, $cmd[1]);
            } elseif ($cmd[1] === 'delete') {
                knje390mQuery::getDeleteFamilyQuery($db, $this);
                unset($this->getRelano);
                unset($this->subfield);
            }
            $this->cmd = $cmd[0]."A";
            $this->setMessage("MSG201");
        }
        Query::dbCheckIn($db);

        return;
    }

    //更新用のモデルを返します(教科等の実態用)
    public function &getUpdateStatusSubclassModel()
    {
        $db = Query::dbCheckOut();

        $cmd = preg_split("{_}", $this->cmd);


        //テーブル名、フィールドデータ取得
        $tablename = $this->gettablename($cmd[0]);
        $fielddata = $this->getfielddata($cmd[0], $cmd[1]);
        $setfield = $this->field2;
        // 科目コード
        list($classCd, $schoolKind, $curriculumCd, $subclassCd) = explode('-', $this->field2['SUBCLASS']);
        $setfield['CLASSCD'] = $classCd;
        $setfield['SCHOOL_KIND'] = $schoolKind;
        $setfield['CURRICULUM_CD'] = $curriculumCd;
        $setfield['SUBCLASSCD'] = $subclassCd;
        //実態と支援
        $setfield['STATUS'] = $setfield['SUBCLASS_STATUS'];
        $setfield['FUTURE_CARE'] = $setfield['SUBCLASS_FUTURE_CARE'];
        //※入力チェックで未入力エラーにならないよう設定(テーブルでの登録・更新では使用しない)
        $this->getRecordSeq = 1;
        //入力チェック
        if ($this->validateInput($db, $setfield, $fielddata, $subfielddata, $cmd[0], $cmd[1])) {
            if ($cmd[1] === 'update' || $cmd[1] === 'insert') {
                $this->getRelano = knje390mQuery::getInsertUpdateStatusSubclassQuery($db, $this, $setfield, $cmd[1]);
            } elseif ($cmd[1] === 'delete') {
                knje390mQuery::getDeleteStatusSubclassQuery($db, $this, $setfield);
            }

            $fieldNo = substr($cmd[0], -1);
            //更新履歴作成
            knje390mQuery::getUpdateLog($db, $this, $tablename, $fieldNo, $cmd[1], "");

            $this->cmd = $cmd[0]."A";
            $this->setMessage("MSG201");
        }
        Query::dbCheckIn($db);

        return;
    }

    //更新用のモデルを返します(実態表->実習の記録)
    public function &getUpdateZittaiJisshuModel()
    {
        $db = Query::dbCheckOut();

        $cmd = preg_split("{_}", $this->cmd);

        //テーブル名、フィールドデータ取得
        $tablename = $this->gettablename($cmd[0]);
        $fielddata = $this->getfielddata($cmd[0], $cmd[1]);
        $setfield = $this->field9;
        // 科目コード
        //実態と支援
        $setfield['STATUS'] = $setfield['SUBCLASS_STATUS'];
        $setfield['FUTURE_CARE'] = $setfield['SUBCLASS_FUTURE_CARE'];
        //※入力チェックで未入力エラーにならないよう設定(テーブルでの登録・更新では使用しない)
        $this->getRecordSeq = 1;
        //入力チェック
        if ($this->validateInput($db, $setfield, $fielddata, $subfielddata, $cmd[0], $cmd[1])) {
            if ($cmd[1] === 'update' || $cmd[1] === 'insert') {
                knje390mQuery::getInsertUpdateZittaiJisshuQuery($db, $this, $setfield, $cmd[1]);
            } elseif ($cmd[1] === 'delete') {
                knje390mQuery::getDeleteZittaiJisshuQuery($db, $this, $setfield);
            }

            $this->cmd = $cmd[0]."A";
            $this->setMessage("MSG201");
        }
        Query::dbCheckIn($db);

        return;
    }
    
    //更新用のモデルを返します(支援計画)
    public function &getUpdateSupportplanModel()
    {
        $db = Query::dbCheckOut();
        $cmd = preg_split("{_}", $this->cmd);

        //フィールドデータ取得
        $fielddata = array(); //作成者・障害名等・合理的
        $subfielddata = array(); //願い・目標
        $setfield = $this->field3;
        if ($setfield["RECORD_DATE"]) {
            //データ新規作成時
            $this->record_date = $setfield["RECORD_DATE"];
            $this->record_date = str_replace("/", "-", $this->record_date);
        }
        $setfield["WRITING_DATE"] = $this->record_date;

        //作成者・障害名等・合意的配慮
        $fielddata = array("WRITING_DATE"               => array("moji" => 10, "gyou" => 1, "title" => "作成年月日", "type" => "DATE"),
                           "RECORD_STAFFNAME"           => array("moji" => 25, "gyou" => 1, "title" => "作成者", "type" => "TEXT"),
                           "CHALLENGED_NAMES"           => array("moji" => 40, "gyou" => 2, "title" => "障害名等", "type" => "TEXT"),
                           "REASONABLE_ACCOMMODATION"   => array("moji" => 55, "gyou" => 15, "title" => "合理的配慮", "type" => "TEXT"),
                           );

        //願い
        $statusName1 = ""; //願い 横項目（左）
        $statusName2 = ""; //願い 横項目（右）
        $query = knje390mQuery::getChallengedSupportplanKindNameDat($this, "01");
        $result = $db->query($query);
        $i = 0;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $seq = $row["KIND_SEQ"];
            if ($seq == "000") {
                $statusName1 = $row["STATUS_NAME1"];
                $statusName2 = $row["STATUS_NAME2"];
            } else {
                $subfielddata["DIV".$i."_HOPE1"] = array("moji" => 25, "gyou" => 3,  "title" => $statusName1."列 ".$row["KIND_NAME"], "type" => "TEXT");
                $subfielddata["DIV".$i."_HOPE2"] = array("moji" => 25, "gyou" => 3,  "title" => $statusName2."列 ".$row["KIND_NAME"], "type" => "TEXT");
            }
            $i++;
        }
        $result->free();

        //目標
        $query = knje390mQuery::getChallengedSupportplanKindNameDat($this, "02");
        $result = $db->query($query);
        $i = 0;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $seq = $row["KIND_SEQ"];
            if ($seq != "000") {
                $subfielddata["DIV".$i."_GOALS"] = array("moji" => 50, "gyou" => 3,  "title" => $row["KIND_NAME"], "type" => "TEXT");
            }
            $i++;
        }
        $result->free();

        //※入力チェックで未入力エラーにならないよう設定(テーブルでの登録・更新では使用しない)
        $this->getRecordSeq = 1;
        //入力チェック
        if ($this->validateInput($db, $setfield, $fielddata, $subfielddata, $cmd[0], $cmd[1])) {

            //作成者・障害名等・合理的配慮
            $fieldNo = substr($cmd[0], -1);
            $tablename = 'SCHREG_CHALLENGED_SUPPORTPLAN_MAIN_DAT';
            knje390mQuery::getUpdateMainQuery($db, $this, $setfield, $tablename, $fielddata, $fieldNo, $cmd[0], $cmd[1], $cmd[2]);

            //願い・目標
            knje390mQuery::getInsertUpdateSupportplanRecordQuery($db, $this);

            $this->cmd = $cmd[0]."A";
            $this->setMessage("MSG201");
        }
        Query::dbCheckIn($db);

        return;
    }
    
    //更新用のモデルを返します(支援計画->具体的な支援、連携の記録)
    public function &getUpdateConcreteSupportModel()
    {
        $db = Query::dbCheckOut();
        $cmd = preg_split("{_}", $this->cmd);

        //フィールドデータ取得
        $fielddata = array(); //連携の記録
        $subfielddata = array(); //具体的な支援
        $setfield = $this->field3;
        if ($setfield["RECORD_DATE"]) {
            //データ新規作成時
            $this->record_date = $setfield["RECORD_DATE"];
            $this->record_date = str_replace("/", "-", $this->record_date);
        }
        $setfield["WRITING_DATE"] = $this->record_date;

        // array("moji" => 50, "gyou" => 3,  "title" => $row["KIND_NAME"], "type" => "TEXT");
        //連携の記録
        $fielddata = array("WRITING_DATE"           => array("moji" => 10, "gyou" => 1, "title" => "作成年月日", "type" => "DATE"),
                           "RECORD"                 => array("moji" => 55, "gyou" => 15, "title" => "連携の記録", "type" => "TEXT"),
                           );

        //具体的な支援
        $statusName1 = ""; //具体的な支援 横項目（左）
        $statusName2 = ""; //具体的な支援 横項目（中央）
        $statusName3 = ""; //具体的な支援 横項目（右）
        $query = knje390mQuery::getChallengedSupportplanKindNameDat($this, "03");
        $result = $db->query($query);
        $i = 0;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $seq = $row["KIND_SEQ"];
            if ($seq == "000") {
                $statusName1 = $row["STATUS_NAME1"];
                $statusName2 = $row["STATUS_NAME2"];
                $statusName3 = $row["STATUS_NAME3"];
            } else {
                $subfielddata["DIV".$i."_STATUS"]  = array("moji" => 15, "gyou" => 15, "title" => $statusName1."列 ".$row["KIND_NAME"], "type" => "TEXT");
                $subfielddata["DIV".$i."_STATUS2"] = array("moji" => 12, "gyou" => 15, "title" => $statusName2."列 ".$row["KIND_NAME"], "type" => "TEXT");
                $subfielddata["DIV".$i."_STATUS3"] = array("moji" => 12, "gyou" => 15, "title" => $statusName3."列 ".$row["KIND_NAME"], "type" => "TEXT");
            }
            $i++;
        }
        $result->free();

        //※入力チェックで未入力エラーにならないよう設定(テーブルでの登録・更新では使用しない)
        $this->getRecordSeq = 1;
        //入力チェック
        if ($this->validateInput($db, $setfield, $fielddata, $subfielddata, $cmd[0], $cmd[1])) {

            //連携の記録
            $fieldNo = substr($cmd[0], -1);
            $tablename = 'SCHREG_CHALLENGED_SUPPORTPLAN_MAIN_DAT';
            knje390mQuery::getUpdateMainQuery($db, $this, $setfield, $tablename, $fielddata, $fieldNo, $cmd[0], $cmd[1], $cmd[2]);

            //具体的な支援
            knje390mQuery::getInsertUpdateConcreteSupportQuery($db, $this);

            $this->cmd = $cmd[0]."A";
            $this->setMessage("MSG201");
        }
        Query::dbCheckIn($db);

        return;
    }


    //対象のテーブル名取得
    public function gettablename($cmd)
    {
        //基本情報
        if ($cmd === 'subform1') {
            $tablename = 'SCHREG_CHALLENGED_PROFILE_MAIN_DAT';
        } elseif ($cmd === 'educate1') {
            $tablename = 'SCHREG_CHALLENGED_PROFILE_EDUCATE_RECORD_DAT';
        } elseif ($cmd === 'medical1') {
            $tablename = 'SCHREG_CHALLENGED_PROFILE_MEDICAL_RECORD_DAT';
        } elseif ($cmd === 'healthcare1') {
            $tablename = 'SCHREG_CHALLENGED_PROFILE_HEALTHCARE_RECORD_DAT';
        } elseif ($cmd === 'welfare1') {
            $tablename = 'SCHREG_CHALLENGED_PROFILE_WELFARE_RECORD_DAT';
        } elseif ($cmd === 'work1') {
            $tablename = 'SCHREG_CHALLENGED_PROFILE_WORK_RECORD_DAT';
        } elseif ($cmd === 'growth1') {
            $tablename = 'SCHREG_CHALLENGED_PROFILE_OTHER_RECORD_DAT';
        } elseif ($cmd === 'training1') {
            $tablename = 'SCHREG_CHALLENGED_PROFILE_OTHER_RECORD_DAT';
        } elseif ($cmd === 'visionEar1') {
            $tablename = 'MEDEXAM_DET_DAT';
        //アセスメント
        } elseif ($cmd === 'subform2') {
            $tablename = 'SCHREG_CHALLENGED_ASSESSMENT_MAIN_DAT';
        } elseif ($cmd === 'check2') {
            $tablename = 'SCHREG_CHALLENGED_ASSESSMENT_CHECK_RECORD_DAT';
        } elseif ($cmd === 'actual2') {
            $tablename = 'SCHREG_CHALLENGED_ASSESSMENT_STATUS_SUBCLASS_DAT';
        //移行支援計画
        } elseif ($cmd === 'subform4') {
            $tablename = 'SCHREG_CHALLENGED_TRANSITION_SUPPORTPLAN_MAIN_DAT';
        } elseif ($cmd === 'check4') {
            $tablename = 'SCHREG_CHALLENGED_TRANSITION_SUPPORTPLAN_CHECK_RECORD_DAT';
        } elseif ($cmd === 'smooth4') {
            if ($this->field4["DATA_DIV"] === '1') {
                $tablename = 'SCHREG_CHALLENGED_TRANSITION_SUPPORTPLAN_MAIN_DAT';
            } else {
                $tablename = 'SCHREG_CHALLENGED_TRANSITION_SUPPORTPLAN_CONSULTATION_RECORD_DAT';
            }
        } elseif ($cmd === 'careerguidance4') {
            $tablename = 'SCHREG_CHALLENGED_TRANSITION_SUPPORTPLAN_CAREERGUIDANCE_RECORD_DAT';
        //サポートブック
        } elseif ($cmd === 'subform5') {
            $tablename = 'SCHREG_CHALLENGED_SUPPORTBOOK_MAIN_DAT';
        //引継資料(担任)
        } elseif ($cmd === 'subform6') {
            $tablename = 'SCHREG_CHALLENGED_TEACHER_TAKEOVER_MAIN_DAT';
        //引継資料(事業者)
        } elseif ($cmd === 'subform7') {
            $tablename = 'SCHREG_CHALLENGED_OPERATOR_TAKEOVER_MAIN_DAT';
        //関係者間資料
        } elseif ($cmd === 'subform8') {
            $tablename = 'SCHREG_CHALLENGED_OFFICIAL_DATA_MAIN_DAT';
        //実態表
        } elseif ($cmd === 'subformZittai') {
            $tablename = 'SCHREG_CHALLENGED_STATUSSHEET_MAIN_DAT';
        //実態表(実習の記録)
        } elseif ($cmd === 'subformZittaiJisshu') {
            $tablename = 'SCHREG_CHALLENGED_STATUSSHEET_TRAINING_DAT';
        }
        return $tablename;
    }
    
    //対象のフィールド情報取得
    public function getfielddata($cmd, $cmd1, $cmd2 = "")
    {
        if ($cmd === 'family') {
            //SCHREG_RELA_DAT
            $fielddata = array("RELANAME"           => array("moji" => 20, "gyou" => 1, "title" => "氏名", "type" => "TEXT"),
                             "RELAKANA"             => array("moji" => 40, "gyou" => 1, "title" => "氏名かな", "type" => "TEXT"),
                             "RELATIONSHIP"         => array("moji" =>  2, "gyou" => 0, "title" => "続柄", "type" => "TEXT"),
                             "REMARK"               => array("moji" => 15, "gyou" => 1, "title" => "備考", "type" => "TEXT"));
        } elseif ($cmd === 'subform1') {
            //メイン
            //SCHREG_CHALLENGED_PROFILE_MAIN_DAT
            $fielddata = array("WRITING_DATE"       => array("moji" => 10, "gyou" => 0, "title" => "作成年月日", "type" => "DATE"),
                             "RECORD_STAFFNAME"     => array("moji" => 25, "gyou" => 1, "title" => "作成者", "type" => "TEXT"),
                             "TSUUGAKU_DIV1"        => array("moji" =>  1, "gyou" => 0, "title" => "通学方法1", "type" => "TEXT"),
                             "TSUUGAKU_DIV2"        => array("moji" =>  1, "gyou" => 0, "title" => "通学方法2", "type" => "TEXT"),
                             "TSUUGAKU_DIV1_REMARK" => array("moji" => 35, "gyou" => 2, "title" => "通学方法1備考", "type" => "TEXT"),
                             "TSUUGAKU_DIV2_REMARK" => array("moji" => 35, "gyou" => 2, "title" => "通学方法2備考", "type" => "TEXT"),
                             "CHALLENGED_NAMES"     => array("moji" => 40, "gyou" => 2, "title" => "障害名等", "type" => "TEXT"),
                             "CHALLENGED_STATUS"    => array("moji" => 40, "gyou" => 3, "title" => "実態概要･障害の特性", "type" => "TEXT"),

                             "CHALLENGED_CARD_NAME"         => array("moji" => 135, "gyou" => 0, "title" => "療育手帳", "type" => "TEXT"),
                             "CHALLENGED_CARD_CLASS"        => array("moji" => 2, "gyou" => 0, "title" => "身体障害手帳の種別", "type" => "TEXT"),
                             "CHALLENGED_CARD_RANK"         => array("moji" => 2, "gyou" => 0, "title" => "身体障害手帳の等級", "type" => "TEXT"),
                             "CHALLENGED_CARD_AREA_NAME"    => array("moji" => 2, "gyou" => 0, "title" => "身体障害手帳の障害名", "type" => "TEXT"),
                             "CHALLENGED_CARD_AREA_NAME2"   => array("moji" => 2, "gyou" => 0, "title" => "身体障害手帳の障害名2", "type" => "TEXT"),
                             "CHALLENGED_CARD_AREA_NAME3"   => array("moji" => 2, "gyou" => 0, "title" => "身体障害手帳の障害名3", "type" => "TEXT"),
                             "CHALLENGED_CARD_REMARK"       => array("moji" => 2, "gyou" => 0, "title" => "精神障害者保健福祉手帳の等級", "type" => "TEXT"),
                             "CHALLENGED_CARD_CHECK_YM"     => array("moji" => 7, "gyou" => 0, "title" => "療育手帳の次回判定", "type" => "DATE"),
                             "CHALLENGED_CARD_GRANT_FLG"    => array("moji" => 1, "gyou" => 0, "title" => "身体障害手帳の次回認定", "type" => "TEXT"),
                             "CHALLENGED_CARD_GRANT_YM"     => array("moji" => 7, "gyou" => 0, "title" => "身体障害手帳の次回認定月", "type" => "TEXT"),
                             "CHALLENGED_CARD_BAST_YM"      => array("moji" => 7, "gyou" => 0, "title" => "精神障害者保健福祉手帳の有効期限", "type" => "DATE"),

                             "WELFARE_MEDICAL_RECEIVE_FLG"      => array("moji" => 1, "gyou" => 0, "title" => "受給者証有無", "type" => "TEXT"),
                             "WELFARE_MEDICAL_RECEIVE_DIV"      => array("moji" => 2, "gyou" => 0, "title" => "種類", "type" => "TEXT"),
                             "WELFARE_MEDICAL_RECEIVE_DIV2"     => array("moji" => 2, "gyou" => 0, "title" => "種類2", "type" => "TEXT"),
                             "WELFARE_MEDICAL_RECEIVE_DIV3"     => array("moji" => 2, "gyou" => 0, "title" => "種類3", "type" => "TEXT"),
                             "WELFARE_MEDICAL_RECEIVE_DIV4"     => array("moji" => 2, "gyou" => 0, "title" => "種類4", "type" => "TEXT"),
                             "WELFARE_MEDICAL_RECEIVE_DIV5"     => array("moji" => 2, "gyou" => 0, "title" => "種類5", "type" => "TEXT"),
                             "P_SCHOOL_CD"                      => array("moji" => $this->finschoolcdKeta, "gyou" => 0, "title" => "小学校", "type" => "TEXT"),
                             "J_SCHOOL_CD"                      => array("moji" => $this->finschoolcdKeta, "gyou" => 0, "title" => "中学校", "type" => "TEXT"),
                             "EVACUATION_AREA"                  => array("moji" => 20, "gyou" => 1, "title" => "避難場所", "type" => "TEXT"),
                             "IMPORTANT_NOTICE"                 => array("moji" => 40, "gyou" => 3, "title" => "留意事項", "type" => "TEXT"),
                             "REMARK"                           => array("moji" => 50, "gyou" => 10, "title" => "備考", "type" => "TEXT"));
        } elseif ($cmd === 'educate1') {
            //療育・教育歴
            //SCHREG_CHALLENGED_PROFILE_EDUCATE_RECORD_DAT
            $fielddata = array("RECORD_DIV"             => array("moji" =>  1, "gyou" => 0, "title" => "種別", "type" => "TEXT"),
                               "RECORD_NO"              => array("moji" =>  1, "gyou" => 0, "title" => "区分", "type" => "TEXT"),
                               "SCHOOL_NAME"            => array("moji" => 20, "gyou" => 1, "title" => "学校名", "type" => "TEXT"),
                               "P_J_SCHOOL_CD"          => array("moji" => $this->finschoolcdKeta, "gyou" => 0, "title" => "保育所・幼稚園・学校名等", "type" => "TEXT"),
                               "S_YEAR_MONTH"           => array("moji" =>  7, "gyou" => 0, "title" => "開始年月", "type" => "TEXT"),
                               "E_YEAR_MONTH"           => array("moji" =>  7, "gyou" => 0, "title" => "終了年月", "type" => "TEXT"),
                               "MAJOR_NAME"             => array("moji" => 30, "gyou" => 1, "title" => "学部名", "type" => "TEXT"),
                               "GRADE"                  => array("moji" => 30, "gyou" => 1, "title" => "学年", "type" => "TEXT"),
                               "HR_CLASS_NAME"          => array("moji" => 30, "gyou" => 1, "title" => "組名", "type" => "TEXT"),
                               "CLASS_SHUBETSU"         => array("moji" =>  2, "gyou" => 0, "title" => "学級種別", "type" => "TEXT"),
                               "REMARK"                 => array("moji" => 25, "gyou" => 1, "title" => "学部・学年・支援学級在籍等", "type" => "TEXT"));
        } elseif ($cmd === 'medical1') {
            //医療
            //SCHREG_CHALLENGED_PROFILE_MEDICAL_RECORD_DAT
            $fielddata = array("RECORD_DIV"             => array("moji" =>  1, "gyou" => 0, "title" => "種別", "type" => "TEXT"),
                               "NAMECD"                 => array("moji" =>  3, "gyou" => 0, "title" => "診療科", "type" => "TEXT"),
                               "CENTER_NAME"            => array("moji" => 30, "gyou" => 1, "title" => "医療機関名", "type" => "TEXT"),
                               "DISEASE_NAME"           => array("moji" => 48, "gyou" => 1, "title" => "病名", "type" => "TEXT"),
                               "ATTEND_STATUS"          => array("moji" => 10, "gyou" => 3, "title" => "担当医師・通院の状況・既往歴等", "type" => "TEXT"));
        } elseif ($cmd === 'healthcare1') {
            //健康管理
            //SCHREG_CHALLENGED_PROFILE_HEALTHCARE_RECORD_DAT
            if ($cmd2 === 'allergen') {
                $fielddata = array("RECORD_DIV"                 => array("moji" =>  1, "gyou" => 0, "title" => "種別", "type" => "TEXT"),
                                   "ALLERGIA_FOOD_CAT"          => array("moji" => 35, "gyou" => 1, "title" => "除去する食品", "type" => "TEXT"),
                                   "ALLERGIA_PLAN"              => array("moji" =>  1, "gyou" => 0, "title" => "取組プラン有無", "type" => "TEXT"),
                                   "ALLERGIA_SPECIAL_REPORT"    => array("moji" => 40, "gyou" => 1, "title" => "特記事項", "type" => "TEXT"),
                                   "ALLERGIA_FOOD_STYLE"        => array("moji" => 40, "gyou" => 1, "title" => "食形態", "type" => "TEXT"),
                                   "ALLERGIA_REMARK"            => array("moji" => 40, "gyou" => 1, "title" => "その他のアレルギー", "type" => "TEXT"));
            } elseif ($cmd2 === 'spasm') {
                $fielddata = array("RECORD_DIV"         => array("moji" =>  1, "gyou" => 0,  "title" => "種別", "type" => "TEXT"),
                                   "REMARK1"            => array("moji" => 15, "gyou" => 1, "title" => "頻度", "type" => "TEXT"),
                                   "REMARK2"            => array("moji" => 15, "gyou" => 1, "title" => "症状", "type" => "TEXT"),
                                   "REMARK3"            => array("moji" => 15, "gyou" => 4, "title" => "発作時の対応", "type" => "TEXT"));
            } elseif ($cmd2 === 'care') {
                $fielddata = array("RECORD_DIV"             => array("moji" => 1, "gyou" => 0, "title" => "種別", "type" => "TEXT"),
                                   "MEDICAL_NAMECD"         => array("moji" => 5, "gyou" => 0, "title" => "医療的ケアの名称", "type" => "TEXT"),
                                   "SCHOOL_CARE"            => array("moji" => 1, "gyou" => 0, "title" => "学校", "type" => "TEXT"),
                                   "HOUSE_CARE"             => array("moji" => 1, "gyou" => 0, "title" => "家庭・病院", "type" => "TEXT"),
                                   "CENTER_CARE"            => array("moji" => 1, "gyou" => 0, "title" => "事業所", "type" => "TEXT"));
            } elseif ($cmd2 === 'consid') {
                $fielddata = array("RECORD_DIV"         => array("moji" =>  1, "gyou" => 0, "title" => "種別", "type" => "TEXT"),
                                   "REMARK4"            => array("moji" => 42, "gyou" => 10, "title" => "配慮事項", "type" => "TEXT"));
            } else {
                $fielddata = array("RECORD_DIV"             => array("moji" =>  1, "gyou" => 0, "title" => "種別", "type" => "TEXT"),
                                   "MEDICINE_NAME"          => array("moji" => 16, "gyou" => 1, "title" => "薬品名", "type" => "TEXT"),
                                   "DISEASE_CONDITION_NAME" => array("moji" => 15, "gyou" => 3, "title" => "病名・症状", "type" => "TEXT"),
                                   "CARE_WAY"               => array("moji" => 15, "gyou" => 6, "title" => "服用状況・留意点", "type" => "TEXT"));
            }
        } elseif ($cmd === 'welfare1') {
            //福祉
            //SCHREG_CHALLENGED_PROFILE_WELFARE_RECORD_DAT
            $fielddata = array("RECORD_DIV"             => array("moji" => 1, "gyou" => 0, "title" => "種別", "type" => "TEXT"),
                               "RECORD_NO"              => array("moji" => 1, "gyou" => 0, "title" => "区分", "type" => "TEXT"),
                               "ITEMCD"                 => array("moji" => 2, "gyou" => 0, "title" => "支給量 数値", "type" => "TEXT"),
                               "ITEMCD2"                => array("moji" => 1, "gyou" => 0, "title" => "支給量 単位", "type" => "TEXT"),
                               "SERVICE_CHARGE"         => array("moji" => 4, "gyou" => 1, "title" => "担当者", "type" => "TEXT"),
                               "SUPPLY_DATE"            => array("moji" => 10, "gyou" => 0, "title" => "申請年月", "type" => "TEXT"));
            if ($this->getRecordDiv == "1") {
                $fielddata["SERVICE_CENTER_TEXT"] = array("moji" => 10, "gyou" => 1, "title" => "制作会社", "type" => "TEXT");
                $fielddata["WELFARE_REMARK"] = array("moji" => 30, "gyou" => 1, "title" => "備考", "type" => "TEXT");
            } elseif ($this->getRecordDiv == "2") {
                $fielddata["SERVICE_CENTER_TEXT"] = array("moji" => 50, "gyou" => 1, "title" => "相談支援事業所", "type" => "TEXT");
                $fielddata["WELFARE_REMARK"] = array("moji" => 40, "gyou" => 1, "title" => "備考","type" =>  "TEXT");
            } elseif ($this->getRecordDiv == "3") {
                $fielddata["SERVICE_CENTER_TEXT"] = array("moji" => 50, "gyou" => 2, "title" => "福祉サービス施設", "type" => "TEXT");
                $fielddata["WELFARE_REMARK"] = array("moji" => 30, "gyou" => 1, "title" => "頻度", "type" => "TEXT");
            }
        } elseif ($cmd === 'work1') {
            //労働
            //SCHREG_CHALLENGED_PROFILE_WORK_RECORD_DAT
            $fielddata = array("CENTERCD"               => array("moji" => 5, "gyou" => 1, "title" => "事業所コード", "type" => "TEXT"),
                               "WORK_REMARK"            => array("moji" => 40, "gyou" => 1, "title" => "利用内容", "type" => "TEXT"));
        } elseif ($cmd == 'growth1') {
            //生育歴
            $fielddata = array("REMARK6"               => array("moji" => 35, "gyou" => 15, "title" => "生育歴", "type" => "TEXT"));
        } elseif ($cmd == 'training1') {
            //訓練機関
            $fielddata = array("REMARK1"               => array("moji" => 40, "gyou" => 4, "title" => "訓練機関", "type" => "TEXT"));
        } elseif ($cmd == 'visionEar1') {
            $fielddata = array("R_BAREVISION"                     =>  array("moji" => 4, "gyou" => 0, "title" => "裸眼右", "type" => "TEXT"),
                               "R_VISION"                         =>  array("moji" => 4, "gyou" => 0, "title" => "矯正右", "type" => "TEXT"),
                               "L_BAREVISION"                     =>  array("moji" => 4, "gyou" => 0, "title" => "裸眼左", "type" => "TEXT"),
                               "L_VISION"                         =>  array("moji" => 4, "gyou" => 0, "title" => "矯正左", "type" => "TEXT"),
                               "R_EAR_DB"                         =>  array("moji" => 3, "gyou" => 0, "title" => "聴力右", "type" => "NUMBER"),
                               "R_EAR"                            =>  array("moji" => 2, "gyou" => 0, "title" => "聴力右（状態）", "type" => "NUMBER"),
                               "L_EAR_DB"                         =>  array("moji" => 3, "gyou" => 0, "title" => "聴力左", "type" => "NUMBER"),
                               "L_EAR"                            =>  array("moji" => 2, "gyou" => 0, "title" => "聴力右（状態）", "type" => "NUMBER"),
                               "DATE"                             =>  array("moji" => 10, "gyou" => 0, "title" => "健康診断実施日", "type" => "DATE"));
        } elseif ($cmd === 'subform2') {
            //メイン
            //SCHREG_CHALLENGED_ASSESSMENT_MAIN_DAT
            $challenged_names_moji = ($this->Properties["useFormNameE390_D_1"] == "KNJE390M_D_1_2.frm") ? 40 : 40;
            $fielddata = array("WRITING_DATE"                   => array("moji" => 10, "gyou" => 1, "title" => "作成年月日", "type" => "DATE"),
                               "CHALLENGED_NAMES"               => array("moji" => $challenged_names_moji, "gyou" => 2, "title" => "障害名等", "type" => "TEXT"),
                               "CHALLENGED_STATUS"              => array("moji" => 40, "gyou" => 3, "title" => "障害の実態･特性", "type" => "TEXT"),
                               "RECORD_STAFFNAME"               => array("moji" => 25, "gyou" => 1, "title" => "作成者", "type" => "TEXT")
                               );
        } elseif ($cmd === 'check2') {
            //発達検査
            //SCHREG_CHALLENGED_ASSESSMENT_CHECK_RECORD_DAT
            $fielddata = array("CHECK_DATE"             => array("moji" => 10, "gyou" => 0, "title" => "検査日", "type" => "DATE"),
                               "CHECK_CENTER_TEXT"      => array("moji" => 50, "gyou" => 2, "title" => "検査機関", "type" => "TEXT"),
                               "CENTERCD"               => array("moji" => 5, "gyou" => 0, "title" => "検査機関コード", "type" => "TEXT"),
                               "CHECK_NAME"             => array("moji" => 10, "gyou" => 2, "title" => "検査名", "type" => "TEXT"));
        } elseif ($cmd === 'actual2') {
            //教科等の実態
            //SCHREG_CHALLENGED_ASSESSMENT_STATUS_SUBCLASS_DAT
            //  YEAR, SCHREGNO, RECORD_DATE, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, STATUS, FUTURE_CARE
            $fielddata = array("CLASSCD"                => array("moji" => 2, "gyou" => 0, "title" => "教科コード", "type" => "TEXT")
                               , "SCHOOL_KIND"          => array("moji" => 2, "gyou" => 0, "title" => "学校校種", "type" => "TEXT")
                               , "CURRICULUM_CD"        => array("moji" => 2, "gyou" => 0, "title" => "教育家庭コード", "type" => "TEXT")
                               , "SUBCLASSCD"           => array("moji" => 6, "gyou" => 0, "title" => "科目コード", "type" => "TEXT")
                               , "STATUS"               => array("moji" => 25, "gyou" => 30, "title" => "実態", "type" => "TEXT")
                               , "FUTURE_CARE"          => array("moji" => 15, "gyou" => 30, "title" => "支援", "type" => "TEXT")
                            );
        } elseif ($cmd === 'subform4') {
            //メイン
            //SCHREG_CHALLENGED_TRANSITION_SUPPORTPLAN_CHECK_RECORD_DAT
            $fielddata = array("WRITING_DATE"                   => array("moji" => 10, "gyou" => 0, "title" => "作成年月日", "type" => "DATE"),
                               "RECORD_STAFFNAME"               => array("moji" => 25, "gyou" => 1, "title" => "作成者", "type" => "TEXT"),
                               "CHALLENGED_NAMES"               => array("moji" => 15, "gyou" => 3, "title" => "障害名･診断名", "type" => "TEXT"),
                               "CHALLENGED_STATUS"              => array("moji" => 15, "gyou" => 8, "title" => "実態概要・障害の特性", "type" => "TEXT"),
                               "ONES_HOPE_PRESENT"              => array("moji" => 20, "gyou" => 2, "title" => "本人　現在　希望・願い", "type" => "TEXT"),
                               "ONES_HOPE_CAREER"               => array("moji" => 20, "gyou" => 2, "title" => "本人　進路　希望・願い", "type" => "TEXT"),
                               "ONES_HOPE_AFTER_GRADUATION"     => array("moji" => 20, "gyou" => 2, "title" => "本人　卒業後　希望・願い", "type" => "TEXT"),
                               "GUARDIAN_HOPE_PRESENT"          => array("moji" => 25, "gyou" => 2, "title" => "保護者　現在　希望・願い", "type" => "TEXT"),
                               "GUARDIAN_HOPE_CAREER"           => array("moji" => 25, "gyou" => 2, "title" => "保護者　進路　希望・願い", "type" => "TEXT"),
                               "GUARDIAN_HOPE_AFTER_GRADUATION" => array("moji" => 25, "gyou" => 2, "title" => "保護者　卒業後　希望・願い", "type" => "TEXT"),
                               "TAKEOVER"                       => array("moji" => 45, "gyou" => 5, "title" => "引継ぎ事項", "type" => "TEXT"));
        } elseif ($cmd === 'check4') {
            //発達検査
            //SCHREG_CHALLENGED_TRANSITION_SUPPORTPLAN_CHECK_RECORD_DAT
            $fielddata = array("CHECK_DATE"             => array("moji" => 10, "gyou" => 0, "title" => "検査日", "type" => "DATE"),
                               "CHECK_REMARK"           => array("moji" => 28, "gyou" => 10, "title" => "検査結果・所見等", "type" => "TEXT"));
        } elseif ($cmd === 'smooth4') {
            if ($this->field4["DATA_DIV"] === '1') {
                //医療・福祉・労働・家庭地域生活
                //SCHREG_CHALLENGED_TRANSITION_SUPPORTPLAN_MAIN_DAT
                $fielddata = array("MEDICAL_GOAL_AFTER_GRADUATION" => array("moji" => 9, "gyou" => 4, "title" => "医療の卒業後の姿・目標", "type" => "TEXT"),
                                   "MEDICAL_STATUS"                => array("moji" => 9, "gyou" => 4, "title" => "医療の実態・課題", "type" => "TEXT"),
                                   "MEDICAL_SHORT_TERM_GOAL"       => array("moji" => 9, "gyou" => 4, "title" => "医療の短期目標", "type" => "TEXT"),
                                   "MEDICAL_TANGIBLE_SUPPORT"      => array("moji" => 9, "gyou" => 4, "title" => "医療の具体的な支援・手立て", "type" => "TEXT"),
                                   "WELFARE_GOAL_AFTER_GRADUATION" => array("moji" => 9, "gyou" => 4, "title" => "福祉の卒業後の姿・目標", "type" => "TEXT"),
                                   "WELFARE_STATUS"                => array("moji" => 9, "gyou" => 4, "title" => "福祉実態・課題", "type" => "TEXT"),
                                   "WELFARE_SHORT_TERM_GOAL"       => array("moji" => 9, "gyou" => 4, "title" => "福祉短期目標", "type" => "TEXT"),
                                   "WELFARE_TANGIBLE_SUPPORT"      => array("moji" => 9, "gyou" => 4, "title" => "福祉具体的な支援・手立て", "type" => "TEXT"),
                                   "WORK_GOAL_AFTER_GRADUATION"    => array("moji" => 9, "gyou" => 4, "title" => "労働の卒業後の姿・目標", "type" => "TEXT"),
                                   "WORK_STATUS"                   => array("moji" => 9, "gyou" => 4, "title" => "労働の実態・課題", "type" => "TEXT"),
                                   "WORK_SHORT_TERM_GOAL"          => array("moji" => 9, "gyou" => 4, "title" => "労働の短期目標", "type" => "TEXT"),
                                   "WORK_TANGIBLE_SUPPORT"         => array("moji" => 9, "gyou" => 4, "title" => "労働の具体的な支援・手立て", "type" => "TEXT"),
                                   "COMMU_GOAL_AFTER_GRADUATION"   => array("moji" => 9, "gyou" => 4, "title" => "家庭地域生活の卒業後の姿・目標", "type" => "TEXT"),
                                   "COMMU_STATUS"                  => array("moji" => 9, "gyou" => 4, "title" => "家庭地域生活の実態・課題", "type" => "TEXT"),
                                   "COMMU_SHORT_TERM_GOAL"         => array("moji" => 9, "gyou" => 4, "title" => "家庭地域生活の短期目標", "type" => "TEXT"),
                                   "COMMU_TANGIBLE_SUPPORT"        => array("moji" => 9, "gyou" => 4, "title" => "家庭地域生活の具体的な支援・手立て", "type" => "TEXT"));
            } else {
                //相談の記録
                //SCHREG_CHALLENGED_TRANSITION_SUPPORTPLAN_CONSULTATION_RECORD_DAT
                $fielddata = array("MEETING_NAME"                  => array("moji" => 10, "gyou" => 4, "title" => "会議名", "type" => "TEXT"),
                                   "MEETING_DATE"                  => array("moji" => 10, "gyou" => 0, "title" => "会議日", "type" => "DATE"),
                                   "TEAM_MEMBERS"                  => array("moji" => 10, "gyou" => 4, "title" => "構成員", "type" => "TEXT"),
                                   "MEETING_SUMMARY"               => array("moji" => 25, "gyou" => 7, "title" => "会議概要", "type" => "TEXT"));
            }
        } elseif ($cmd === 'careerguidance4') {
            //進路指導計画
            //SCHREG_CHALLENGED_TRANSITION_SUPPORTPLAN_CAREERGUIDANCE_RECORD_DAT
            if ($cmd1 !== 'update2') {
                $fielddata = array("RECORD_DIV"                   => array("moji" => 1, "gyou" => 0, "title" => "種別", "type" => "TEXT"),
                                   "RECORD_NO"                    => array("moji" => 1, "gyou" => 0, "title" => "区分", "type" => "TEXT"),
                                   "MEETING_NAME"                 => array("moji" => 40, "gyou" => 1, "title" => "会議名", "type" => "TEXT"),
                                   "MEETING_DATE"                 => array("moji" => 10, "gyou" => 0, "title" => "会議日", "type" => "DATE"),
                                   "TEAM_MEMBERS"                 => array("moji" => 10, "gyou" => 4, "title" => "構成員", "type" => "TEXT"),
                                   "MEETING_SUMMARY"              => array("moji" => 25, "gyou" => 7, "title" => "概要", "type" => "TEXT"),
                                   "WORK_TRAINING_PLACE"          => array("moji" => 20, "gyou" => 1, "title" => "実習、見学場所", "type" => "TEXT"),
                                   "WORK_TRAINING_S_DATE"         => array("moji" => 10, "gyou" => 0, "title" => "実習、見学期日", "type" => "DATE"),
                                   "WORK_TRAINING_E_DATE"         => array("moji" => 10, "gyou" => 0, "title" => "実習、見学期日終了日", "type" => "DATE"),
                                   "WORK_TRAINING_CONTENTS"       => array("moji" => 40, "gyou" => 1, "title" => "実習、見学内容", "type" => "TEXT"),
                                   "WORK_TRAINING_GOAL"           => array("moji" => 35, "gyou" => 5, "title" => "実習、見学目標", "type" => "TEXT"),
                                   "WORK_TRAINING_SUPPORT"        => array("moji" => 31, "gyou" => 5, "title" => "実習、見学支援、手立て", "type" => "TEXT"),
                                   "WORK_TRAINING_SUPPORT_TARGET" => array("moji" => 6, "gyou" => 5,  "title" => "実習、見学支援、手立ての対象", "type" => "TEXT"),
                                   "WORK_TRAINING_RESULT"         => array("moji" => 35, "gyou" => 5, "title" => "実習、見学成果", "type" => "TEXT"),
                                   "WORK_TRAINING_CHALLENGE"      => array("moji" => 35, "gyou" => 5, "title" => "実習、見学課題", "type" => "TEXT"),
                                   "DETERMINED_COURSE"            => array("moji" => 40, "gyou" => 1, "title" => "事業所、福祉利用", "type" => "TEXT"),
                                   "COURSE_CONTENTS"              => array("moji" => 35, "gyou" => 4, "title" => "内容", "type" => "TEXT"));
            } else {
                $fielddata = array("RECORD_DIV"                   => array("moji" => 1, "gyou" => 0, "title" => "種別", "type" => "TEXT"),
                                   "RECORD_NO"                    => array("moji" => 1, "gyou" => 0, "title" => "区分", "type" => "TEXT"),
                                   "CAREER_GUIDANCE_RESULT"       => array("moji" => 35, "gyou" => 5, "title" => "年間の成果", "type" => "TEXT"),
                                   "CAREER_GUIDANCE_CHALLENGE"    => array("moji" => 35, "gyou" => 5, "title" => "年間の課題", "type" => "TEXT"),
                                   "REMARK"                       => array("moji" => 40, "gyou" => 5, "title" => "その他", "type" => "TEXT"));
            }
            //サポートブック
        } elseif ($cmd === 'subform5') {
            //メイン
            //SCHREG_CHALLENGED_SUPPORTBOOK_MAIN_DAT
            $fielddata = array("WRITING_DATE"                 => array("moji" => 10, "gyou" => 0, "title" => "作成年月日", "type" => "DATE"),
                               "CHALLENGED_NAMES"             => array("moji" => 40, "gyou" => 2, "title" => "障害名", "type" => "TEXT"),
                               "CHALLENGED_POPULAR_NAME"      => array("moji" => 40, "gyou" => 2, "title" => "呼び名", "type" => "TEXT"),
                               "CHRONIC_DISEASE"              => array("moji" => 40, "gyou" => 5, "title" => "持病名", "type" => "TEXT"),
                               "HOW_COMMUNICATION"            => array("moji" => 40, "gyou" => 6, "title" => "コミュニケーション(会話)の仕方", "type" => "TEXT"),
                               "TROUBLED_BEHAVIOR_SUPPORT"    => array("moji" => 40, "gyou" => 7, "title" => "困った行動の対処法", "type" => "TEXT"),
                               "BAD_CONDITION_SIGN"           => array("moji" => 40, "gyou" => 7, "title" => "不調のサイン", "type" => "TEXT"));
        //引継資料(担任)
        } elseif ($cmd === 'subform6') {
            //メイン
            //SCHREG_CHALLENGED_TEACHER_TAKEOVER_MAIN_DAT
            $fielddata = array("WRITING_DATE"                 => array("moji" => 10, "gyou" => 0, "title" => "作成年月日", "type" => "DATE"));
        //引継資料(事業者)
        } elseif ($cmd === 'subform7') {
            //メイン
            //SCHREG_CHALLENGED_OPERATOR_TAKEOVER_MAIN_DAT
            $fielddata = array("WRITING_DATE"                 => array("moji" => 10, "gyou" => 0, "title" => "作成年月日", "type" => "DATE"));
        //関係者間資料
        } elseif ($cmd === 'subform8') {
            //メイン
            //SCHREG_CHALLENGED_OFFICIAL_DATA_MAIN_DAT
            $fielddata = array("WRITING_DATE"                 => array("moji" => 10, "gyou" => 0, "title" => "作成年月日", "type" => "DATE"),
                               "WRITER"                       => array("moji" => 18, "gyou" => 1, "title" => "記入者", "type" => "TEXT"),
                               "MEETING_NAME"                 => array("moji" => 40, "gyou" => 1, "title" => "会議名", "type" => "TEXT"),
                               "MEETING_DATE"                 => array("moji" => 10, "gyou" => 0, "title" => "打合せ日", "type" => "DATE"),
                               "MEETING_SHOUR"                => array("moji" => 2, "gyou" => 0, "title" => "打合せ開始時間(時)", "type" => "NUMBER"),
                               "MEETING_SMINUTES"             => array("moji" => 2, "gyou" => 0, "title" => "打合せ開始時間(分)", "type" => "NUMBER"),
                               "MEETING_EHOUR"                => array("moji" => 2, "gyou" => 0, "title" => "打合せ終了時間(時)", "type" => "NUMBER"),
                               "MEETING_EMINUTES"             => array("moji" => 2, "gyou" => 0, "title" => "打合せ終了時間(分)", "type" => "NUMBER"),
                               "MEETING_PALCE"                => array("moji" => 18, "gyou" => 1, "title" => "打合せ場所", "type" => "TEXT"),
                               "MEETING_PARTICIPANT"          => array("moji" => 40, "gyou" => 2, "title" => "参加者", "type" => "TEXT"));
        //実態表
        } elseif ($cmd === 'subformZittai') {
            //SCHREG_CHALLENGED_STATUSSHEET_TRAINING_DAT
            $fielddata = array("WRITING_DATE"                 => array("moji" => 10, "gyou" => 0, "title" => "作成年月日", "type" => "DATE"),
                               "RECORD_STAFFNAME"             => array("moji" => 25, "gyou" => 1, "title" => "担任名", "type" => "TEXT"),
                               "CHALLENGED_NAMES"             => array("moji" => 40, "gyou" => 2, "title" => "障害名等", "type" => "TEXT"),
                               );
        } elseif ($cmd === 'subformZittaiJisshu') {
            //SCHREG_CHALLENGED_STATUSSHEET_TRAINING_DAT
            $fielddata = array("COMPANY_NAME"                 => array("moji" => 45, "gyou" => 1, "title" => "実習先", "type" => "TEXT"),
                               "START_DATE"                   => array("moji" => 10, "gyou" => 0, "title" => "開始日付", "type" => "DATE"),
                               "FINISH_DATE"                  => array("moji" => 10, "gyou" => 0, "title" => "終了日付", "type" => "DATE"),
                               "REMARK"                       => array("moji" => 45, "gyou" => 1, "title" => "備考", "type" => "TEXT"));
        }

        return $fielddata;
    }
    
    //対象のテーブル名取得(メイン画面の別テーブル)
    public function getsubtablename($cmd)
    {
        //アセスメント
        if ($cmd === 'subform2') {
            $subtablename = 'SCHREG_CHALLENGED_ASSESSMENT_STATUS_GROWUP_DAT';
        //移行支援計画
        } elseif ($cmd === 'subform4') {
            $subtablename = 'SCHREG_CHALLENGED_TRANSITION_SUPPORTPLAN_STATUS_DAT';
        // 引継資料(担任者)
        } elseif ($cmd === 'subform6') {
            $subtablename = 'SCHREG_CHALLENGED_TEACHER_TAKEOVER_DOCUMENTS_DAT';
        // 引継資料(事業者)
        } elseif ($cmd === 'subform7') {
            $subtablename = 'SCHREG_CHALLENGED_OPERATOR_TAKEOVER_DOCUMENTS_DAT';
        // 関係者間資料
        } elseif ($cmd === 'subform8') {
            $subtablename = 'SCHREG_CHALLENGED_OFFICIAL_DATA_DOCUMENTS_DAT';
        //基本情報（福祉）
        } elseif ($cmd === 'welfare1') {
            $subtablename = 'SCHREG_CHALLENGED_PROFILE_WELFARE_RECORD_ITEMCD_DAT';
        //基本情報（視力・聴力）
        } elseif ($cmd === 'visionEar1') {
            $subtablename = 'MEDEXAM_DET_DETAIL_DAT';
        //実態表
        } elseif ($cmd === 'subformZittai') {
            $subtablename = 'SCHREG_CHALLENGED_STATUSSHEET_STATUS_GROWUP_DAT';
        }

        return $subtablename;
    }

    //対象のフィールド情報取得(メイン画面の別テーブル)
    public function getsubfielddata($db, $cmd)
    {
        if ($cmd === 'subform2' || $cmd === 'subformZittai') {
            $label = array();
            $maxDataDiv = 0;
            if ($cmd === 'subform2') {
                $query = knje390mQuery::getChallengedAssessmentStatusGrowupDat($this);
                $subform2Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
                if ($subform2Row["DATA_DIV"] == '0' && $subform2Row["SHEET_PATTERN"] == "2") {
                    //2枠パターン
                    $array = array("STATUS" => array("moji" => 25, "gyou" => 30), "GROWUP" => array("moji" => 15, "gyou" => 30));
                } else {
                    //1枠パターン
                    $array = array("STATUS" =>  array("moji" => 40, "gyou" => 30), "GROWUP" => array("moji" => 15, "gyou" => 30));
                }
            } elseif ($cmd === 'subformZittai') {
                $zittaiItemRow = $db->getRow(knje390mQuery::getChallengedStatusSheetItemNameDat($this, "0"), DB_FETCHMODE_ASSOC);
                if ($zittaiItemRow["GROWUP_NAME"] != "") {             //1枠or2枠パターンで入力サイズが異なる
                    $array = array("STATUS" => array("moji" => 25, "gyou" => 30), "GROWUP" => array("moji" => 15, "gyou" => 30));
                } else {
                    $array = array("STATUS" => array("moji" => 40, "gyou" => 30));
                }
                $query = knje390mQuery::getChallengedStatusSheetItemNameDat($this, "");
            }
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($row["DATA_DIV"] == "0") {
                    foreach ($array as $keyField => $size) {
                        $label[$keyField."_NAME"] = $row[$keyField."_NAME"];
                    }
                } else {
                    $label["DATA_DIV_NAME".$row["DATA_DIV"]] = $row["DATA_DIV_NAME"];
                    $maxDataDiv = $row["DATA_DIV"];
                }
            }
            $result->free();

            $subfielddata = array();
            if ($maxDataDiv > 0) {
                for ($i = 1; $i <= $maxDataDiv; $i++) {
                    foreach ($array as $keyField => $size) {
                        $sep = (strlen($label["DATA_DIV_NAME".$i]) && strlen($label[$keyField."_NAME"])) ? "・" : "";
                        $msgFieldName = $label["DATA_DIV_NAME".$i].$sep.$label[$keyField."_NAME"];
                        $subfielddata["DIV".$i."_".$keyField] = array("moji" => $size["moji"], "gyou" => $size["gyou"], "title" => $msgFieldName, "type" => "TEXT", "DATA_DIV" => $i);
                    }
                }
            }
        } elseif ($cmd === 'subform4') {
            $subfielddata = array(
                "LIFESTYLE_STATUS"         => array("moji" => 35, "gyou" => 15, "title" => "基本的生活習慣身辺自立の実態", "type" => "TEXT", "DATA_DIV" => "01"),
                "SOCIALITY_STATUS"         => array("moji" => 35, "gyou" => 15, "title" => "行動・社会性の実態", "type" => "TEXT", "DATA_DIV" => "02"),
                "COMMUNICATION_STATUS"     => array("moji" => 35, "gyou" => 15, "title" => "言語コミュニケーションの実態", "type" => "TEXT", "DATA_DIV" => "03"),
                "PHYSICAL_ACTIVITY_STATUS" => array("moji" => 35, "gyou" => 15, "title" => "身体・運動の実態", "type" => "TEXT", "DATA_DIV" => "04"),
                "STUDY_STATUS"             => array("moji" => 35, "gyou" => 15, "title" => "学習の実態", "type" => "TEXT", "DATA_DIV" => "05"),
                "INTERESTING_STATUS"       => array("moji" => 35, "gyou" => 15, "title" => "興味・強みの実態", "type" => "TEXT", "DATA_DIV" => "06")
            );
        //引継資料(担任)
        } elseif ($cmd === 'subform6') {
            $subfielddata = array(
                "DATA_DIV"                => array("moji" => 2, "gyou" => 0, "title" => "種別", "type" => "TEXT"),
                "CAN_BE_NO_SUPPORT"       => array("moji" => 13, "gyou" => 5, "title" => "支援なしでできること", "type" => "TEXT"),
                "CAN_BE_SOME_SUPPORT"     => array("moji" => 13, "gyou" => 5, "title" => "一部支援でできること", "type" => "TEXT"),
                "MEANS"                   => array("moji" => 13, "gyou" => 5, "title" => "手立て", "type" => "TEXT"),
                "SHORT_TERM_GOAL"         => array("moji" => 13, "gyou" => 5, "title" => "短期目標", "type" => "TEXT"),
                "GOAL_FUTURE"             => array("moji" => 13, "gyou" => 5, "title" => "将来に目指す", "type" => "TEXT")
            );
        //引継資料(事業者)
        } elseif ($cmd === 'subform7') {
            $subfielddata = array(
                "DATA_DIV"                => array("moji" => 2, "gyou" => 0, "title" => "種別", "type" => "TEXT"),
                "OUTLINE"                 => array("moji" => 11, "gyou" => 8, "title" => "概要", "type" => "TEXT"),
                "MEASURE"                 => array("moji" => 11, "gyou" => 8, "title" => "方策", "type" => "TEXT"),
                "FUTURE_DEVELOPMENT"      => array("moji" => 11, "gyou" => 8, "title" => "今後の展開", "type" => "TEXT"),
                "REMARK"                  => array("moji" => 11, "gyou" => 8, "title" => "備考", "type" => "TEXT")
            );
        //関係者間資料
        } elseif ($cmd === 'subform8') {
            $subfielddata = array(
                "PROBLEM_EVENT_DOCUMENTS"        => array("moji" => 40, "gyou" => 10, "title" => "問題事象", "type" => "TEXT", "DATA_DIV" => "01"),
                "PROBLEM_ANALYSIS_DOCUMENTS"     => array("moji" => 40, "gyou" => 10, "title" => "事象分析", "type" => "TEXT", "DATA_DIV" => "02"),
                "PROBLEM_DEAL_DOCUMENTS"         => array("moji" => 40, "gyou" => 10, "title" => "課題対応", "type" => "TEXT", "DATA_DIV" => "03"),
                "STAFF_ROLE_DOCUMENTS"           => array("moji" => 40, "gyou" => 10, "title" => "指導者の役割等", "type" => "TEXT", "DATA_DIV" => "04"),
                "GUARDIAN_COOPERATION_DOCUMENTS" => array("moji" => 40, "gyou" => 10, "title" => "保護者連携", "type" => "TEXT", "DATA_DIV" => "05"),
                "WELFARE_COOPERATION_DOCUMENTS"  => array("moji" => 40, "gyou" => 10, "title" => "福祉連携", "type" => "TEXT", "DATA_DIV" => "06"),
                "OTHERS_REMARK_DOCUMENTS"        => array("moji" => 40, "gyou" => 10, "title" => "その他", "type" => "TEXT", "DATA_DIV" => "07")
            );
        //基本情報（福祉）
        } elseif ($cmd === 'welfare1') {
            $subfielddata = array("ITEMCD1"     => array("moji" => 3, "gyou" => 0, "title" => "補装具・日常生活用具", "type" => "TEXT"),
                                  "ITEMCD2"     => array("moji" => 3, "gyou" => 0, "title" => "補装具・日常生活用具", "type" => "TEXT"),
                                  "ITEMCD3"     => array("moji" => 3, "gyou" => 0, "title" => "補装具・日常生活用具", "type" => "TEXT"),
                                  "ITEMCD4"     => array("moji" => 3, "gyou" => 0, "title" => "補装具・日常生活用具", "type" => "TEXT"),
                                  "ITEMCD5"     => array("moji" => 3, "gyou" => 0, "title" => "補装具・日常生活用具", "type" => "TEXT"),
                                  "ITEMCD6"     => array("moji" => 3, "gyou" => 0, "title" => "補装具・日常生活用具", "type" => "TEXT"));
        //基本情報（視力・聴力）
        } elseif ($cmd === 'visionEar1') {
            $subfielddata = array("RL_BAREVISION" =>  array("moji" => 4, "gyou" => 0, "title" => "両眼（裸眼）", "type" => "TEXT"),
                                  "RL_VISION"     =>  array("moji" => 4, "gyou" => 0, "title" => "両眼（強制）", "type" => "TEXT"),
                                  "DET_REMARK3"   =>  array("moji" => 4, "gyou" => 0, "title" => "両耳", "type" => "TEXT"),
                                  "DET_REMARK4"   =>  array("moji" => 4, "gyou" => 0, "title" => "閾値（補聴器装用時）", "type" => "TEXT"),
                                  "DET_REMARK5"   =>  array("moji" => 4, "gyou" => 0, "title" => "閾値（人口内耳）", "type" => "TEXT"),
                                  "DET_REMARK6"   =>  array("moji" => 25, "gyou" => 2, "title" => "メーカー・機種・シリアル番号", "type" => "TEXT"),
                                  "DET_REMARK7"   =>  array("moji" => 25, "gyou" => 1, "title" => "主なコミュニケーション手段", "type" => "TEXT"),
                                  "RL_VISION_CANTMEASURE" =>  array("moji" => 2, "gyou" => 0, "title" => "両眼（測定困難）", "type" => "TEXT")
                                );
        }

        return $subfielddata;
    }
            
    //対象のフィールド西暦変換
    public function getSeirekiHenkan($year_month)
    {
        $setYearMonth = array();
        $setYearMonth = preg_split("{/}", $year_month);
        $gengo = substr($setYearMonth[0], 0, 1);
        $warekiyear = substr($setYearMonth[0], 1, 2);
        $warekiList = common::getWarekiList();
        // 和暦から西暦
        foreach ($warekiList as $w) {
            if ($gengo == $w["SName"]) {
                $seireki = intval($warekiyear) + intval($w["YearStart"]) - 1;
                break;
            }
        }

        $year_month = $seireki.'-'.$setYearMonth[1];
        
        return $year_month;
    }
}
