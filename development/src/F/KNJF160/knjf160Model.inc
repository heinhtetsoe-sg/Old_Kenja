<?php

require_once('for_php7.php');

class knjf160Model extends Model {

    var $cmd;
    var $schregno = "";
    var $name = "";
    var $grade;
    var $hr_class;
    var $attendno;
    var $field = array();
    var $control;

    var $seq;
    var $checked;
    var $relano;
    var $data  = array();

    function knjf160Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {
        if (VARS::get("PROGRAMID") != "") {
            unset($this->schregno);
            unset($this->name);
            unset($this->grade);
            unset($this->hr_class);
            unset($this->attendno);

            unset($this->seq);
            unset($this->relano);
        }

        $this->field = array("SEQ"                      =>  VARS::post("SEQ"),                  //SEQ
                             "DISEASE"                  =>  VARS::post("DISEASE"),              //病名
                             "S_YEAR"                   =>  VARS::post("S_YEAR"),               //開始年
                             "S_MONTH"                  =>  (VARS::post("S_MONTH")) ? intval(VARS::post("S_MONTH")) : "",  //開始月
                             "E_YEAR"                   =>  VARS::post("E_YEAR"),               //終了年
                             "E_MONTH"                  =>  (VARS::post("E_MONTH")) ? intval(VARS::post("E_MONTH")) : "",  //終了月
                             "SITUATION"                =>  VARS::post("SITUATION"),            //経過
                             "HOSPITAL"                 =>  VARS::post("HOSPITAL"),             //病院
                             "DOCTOR"                   =>  VARS::post("DOCTOR"),               //主治医
                             "TELNO"                    =>  VARS::post("TELNO"),                //電話番号
                             "MEDICINE"                 =>  VARS::post("MEDICINE"),             //服用薬
                             "SUSPECT"                  =>  VARS::post("SUSPECT"),              //出席停止

                             "RELANO"                   =>  VARS::post("RELANO"),               //親族番号
                             "RELANAME"                 =>  VARS::post("RELANAME"),             //親族氏名
                             "RELAKANA"                 =>  VARS::post("RELAKANA"),             //親族氏名かな
                             "RELATIONSHIP"             =>  VARS::post("RELATIONSHIP"),         //続柄
                             "RELABIRTHDAY"             =>  VARS::post("RELABIRTHDAY"),         //親族生年月日
                             "REMARK"                   =>  VARS::post("REMARK"),               //親族の健康状態

                             "ALLERGY_MEDICINE"         =>  VARS::post("ALLERGY_MEDICINE"),     //薬アレルギー
                             "ALLERGY_FOOD"             =>  VARS::post("ALLERGY_FOOD"),         //食品アレルギー
                             "ALLERGY_OTHER"            =>  VARS::post("ALLERGY_OTHER"),        //その他アレルギー
                             "BLOOD"                    =>  VARS::post("BLOOD"),                //血液型
                             "RH"                       =>  VARS::post("RH"),                   //Ｒｈ
                             "MEASLES_AGE"              =>  (VARS::post("MEASLES_AGE") == "") ? "" : intval(VARS::post("MEASLES_AGE")),   //麻疹（年齢）
                             "G_MEASLES_AGE"            =>  (VARS::post("G_MEASLES_AGE")  == "") ? "" : intval(VARS::post("G_MEASLES_AGE")),   //風疹（年齢）
                             "VARICELLA_AGE"            =>  (VARS::post("VARICELLA_AGE") == "") ? "" : intval(VARS::post("VARICELLA_AGE")),   //水痘（年齢）
                             "OTITIS_MEDIA_AGE"         =>  (VARS::post("OTITIS_MEDIA_AGE") == "") ? "" : intval(VARS::post("OTITIS_MEDIA_AGE")),   //中耳炎（年齢）
                             "TB_AGE"                   =>  (VARS::post("TB_AGE") == "") ? "" : intval(VARS::post("TB_AGE")),   //結核（年齢）
                             "KAWASAKI_AGE"             =>  (VARS::post("KAWASAKI_AGE") == "") ? "" : intval(VARS::post("KAWASAKI_AGE")),   //川崎病（年齢）
                             "INFECTION_AGE"            =>  (VARS::post("INFECTION_AGE") == "") ? "" : intval(VARS::post("INFECTION_AGE")),   //溶連菌感染症（年齢）
                             "MUMPS_AGE"                =>  (VARS::post("MUMPS_AGE") == "") ? "" : intval(VARS::post("MUMPS_AGE")),   //流行性耳下腺炎（年齢）
                             "HEART_DISEASE"            =>  VARS::post("HEART_DISEASE"),        //心臓疾患（病名）
                             "HEART_S_AGE"              =>  (VARS::post("HEART_S_AGE") == "") ? "" : intval(VARS::post("HEART_S_AGE")),   //心臓疾患（年齢）
                             "HEART_SITUATION"          =>  VARS::post("HEART_SITUATION"),      //心臓疾患（経過）
                             "HEART_E_AGE"              =>  (VARS::post("HEART_E_AGE") == "") ? "" : intval(VARS::post("HEART_E_AGE")),   //心臓疾患（全治年齢）
                             "KIDNEY_DISEASE"           =>  VARS::post("KIDNEY_DISEASE"),       //腎臓疾患（病名）
                             "KIDNEY_S_AGE"             =>  (VARS::post("KIDNEY_S_AGE") == "") ? "" : intval(VARS::post("KIDNEY_S_AGE")),   //腎臓疾患（年齢）
                             "KIDNEY_SITUATION"         =>  VARS::post("KIDNEY_SITUATION"),     //腎臓疾患（経過）
                             "KIDNEY_E_AGE"             =>  (VARS::post("KIDNEY_E_AGE") == "") ? "" : intval(VARS::post("KIDNEY_E_AGE")),   //腎臓疾患（全治年齢）
                             "ASTHMA_S_AGE"             =>  (VARS::post("ASTHMA_S_AGE") == "") ? "" : intval(VARS::post("ASTHMA_S_AGE")),   //ぜんそく（年齢）
                             "ASTHMA_SITUATION"         =>  VARS::post("ASTHMA_SITUATION"),     //ぜんそく（経過）
                             "ASTHMA_E_AGE"             =>  (VARS::post("ASTHMA_E_AGE") == "") ? "" : intval(VARS::post("ASTHMA_E_AGE")),   //ぜんそく（全治年齢）
                             "CONVULSIONS_S_AGE"        =>  (VARS::post("CONVULSIONS_S_AGE") == "") ? "" : intval(VARS::post("CONVULSIONS_S_AGE")),   //けいれん（年齢）
                             "CONVULSIONS_SITUATION"    =>  VARS::post("CONVULSIONS_SITUATION"),//けいれん（経過）
                             "CONVULSIONS_E_AGE"        =>  (VARS::post("CONVULSIONS_E_AGE") == "") ? "" : intval(VARS::post("CONVULSIONS_E_AGE")),   //けいれん（全治年齢）
                             "OTHER_DISEASE"            =>  VARS::post("OTHER_DISEASE"),        //その他（病名）
                             "TUBERCULIN"               =>  VARS::post("TUBERCULIN"),           //ツベルクリン（接種）
                             "TUBERCULIN_YEAR"          =>  VARS::post("TUBERCULIN_YEAR"),      //ツベルクリン（年）
                             "TUBERCULIN_MONTH"         =>  (VARS::post("TUBERCULIN_MONTH")) ? intval(VARS::post("TUBERCULIN_MONTH")) : "",  //ツベルクリン（月）
                             "TUBERCULIN_JUDGE"         =>  VARS::post("TUBERCULIN_JUDGE"),     //ツベルクリン（判定）
                             "BCG"                      =>  VARS::post("BCG"),                  //ＢＣＧ（接種）
                             "BCG_YEAR"                 =>  VARS::post("BCG_YEAR"),             //ＢＣＧ（年）
                             "BCG_MONTH"                =>  (VARS::post("BCG_MONTH")) ? intval(VARS::post("BCG_MONTH")) : "",  //ＢＣＧ（月）
                             "POLIO"                    =>  VARS::post("POLIO"),                //ポリオ（接種）
                             "POLIO_YEAR"               =>  VARS::post("POLIO_YEAR"),           //ポリオ（年）
                             "POLIO_MONTH"              =>  (VARS::post("POLIO_MONTH")) ? intval(VARS::post("POLIO_MONTH")): "",  //ポリオ（月）
                             "G_MEASLES"                =>  VARS::post("G_MEASLES"),            //風疹（接種）
                             "G_MEASLES_YEAR"           =>  VARS::post("G_MEASLES_YEAR"),       //風疹（年）
                             "G_MEASLES_MONTH"          =>  (VARS::post("G_MEASLES_MONTH")) ? intval(VARS::post("G_MEASLES_MONTH")) : "",  //風疹（月）
                             "VARICELLA"                =>  VARS::post("VARICELLA"),            //水痘（接種）
                             "VARICELLA_YEAR"           =>  VARS::post("VARICELLA_YEAR"),       //水痘（年）
                             "VARICELLA_MONTH"          =>  (VARS::post("VARICELLA_MONTH")) ? intval(VARS::post("VARICELLA_MONTH")) : "",  //水痘（月）
                             "MUMPS"                    =>  VARS::post("MUMPS"),                //流行性耳下腺炎（接種）
                             "MUMPS_YEAR"               =>  VARS::post("MUMPS_YEAR"),           //流行性耳下腺炎（年）
                             "MUMPS_MONTH"              =>  (VARS::post("MUMPS_MONTH")) ? intval(VARS::post("MUMPS_MONTH")) : "",  //流行性耳下腺炎（月）
                             "ENCEPHALITIS"             =>  VARS::post("ENCEPHALITIS"),         //日本脳炎（接種）
                             "ENCEPHALITIS_YEAR1"       =>  VARS::post("ENCEPHALITIS_YEAR1"),   //日本脳炎（年）
                             "ENCEPHALITIS_MONTH1"      =>  (VARS::post("ENCEPHALITIS_MONTH1")) ? intval(VARS::post("ENCEPHALITIS_MONTH1")) : "",  //日本脳炎（月）
                             "ENCEPHALITIS_YEAR2"       =>  VARS::post("ENCEPHALITIS_YEAR2"),   //日本脳炎（年）
                             "ENCEPHALITIS_MONTH2"      =>  (VARS::post("ENCEPHALITIS_MONTH2")) ? intval(VARS::post("ENCEPHALITIS_MONTH2")) : "",  //日本脳炎（月）
                             "ENCEPHALITIS_YEAR3"       =>  VARS::post("ENCEPHALITIS_YEAR3"),   //日本脳炎（年）
                             "ENCEPHALITIS_MONTH3"      =>  (VARS::post("ENCEPHALITIS_MONTH3")) ? intval(VARS::post("ENCEPHALITIS_MONTH3")) : "",  //日本脳炎（月）
                             "ENCEPHALITIS_YEAR4"       =>  VARS::post("ENCEPHALITIS_YEAR4"),   //日本脳炎（年）
                             "ENCEPHALITIS_MONTH4"      =>  (VARS::post("ENCEPHALITIS_MONTH4")) ? intval(VARS::post("ENCEPHALITIS_MONTH4")) : "",  //日本脳炎（月）
                             "ENCEPHALITIS_YEAR5"       =>  VARS::post("ENCEPHALITIS_YEAR5"),   //日本脳炎（年）
                             "ENCEPHALITIS_MONTH5"      =>  (VARS::post("ENCEPHALITIS_MONTH5")) ? intval(VARS::post("ENCEPHALITIS_MONTH5")) : "",  //日本脳炎（月）
                             "MIXED"                    =>  VARS::post("MIXED"),                //二種混合（接種）
                             "MIXED_YEAR1"              =>  VARS::post("MIXED_YEAR1"),          //二種混合（年）
                             "MIXED_MONTH1"             =>  (VARS::post("MIXED_MONTH1")) ? intval(VARS::post("MIXED_MONTH1")) : "",  //二種混合（月）
                             "MIXED_YEAR2"              =>  VARS::post("MIXED_YEAR2"),          //二種混合（年）
                             "MIXED_MONTH2"             =>  (VARS::post("MIXED_MONTH2")) ? intval(VARS::post("MIXED_MONTH2")) : "",  //二種混合（月）
                             "MIXED_YEAR3"              =>  VARS::post("MIXED_YEAR3"),          //二種混合（年）
                             "MIXED_MONTH3"             =>  (VARS::post("MIXED_MONTH3")) ? intval(VARS::post("MIXED_MONTH3")) : "",  //二種混合（月）
                             "MIXED_YEAR4"              =>  VARS::post("MIXED_YEAR4"),          //二種混合（年）
                             "MIXED_MONTH4"             =>  (VARS::post("MIXED_MONTH4")) ? intval(VARS::post("MIXED_MONTH4")) : "",  //二種混合（月）
                             "MIXED_YEAR5"              =>  VARS::post("MIXED_YEAR5"),          //二種混合（年）
                             "MIXED_MONTH5"             =>  (VARS::post("MIXED_MONTH5")) ? intval(VARS::post("MIXED_MONTH5")) : "",  //二種混合（月）
                             "MEASLES"                  =>  VARS::post("MEASLES"),              //麻疹（接種）
                             "MEASLES_TIMES"            =>  VARS::post("MEASLES_TIMES"),        //麻疹（回数）
                             "MEASLES_YEAR1"            =>  VARS::post("MEASLES_YEAR1"),        //麻疹（年）
                             "MEASLES_MONTH1"           =>  (VARS::post("MEASLES_MONTH1")) ? intval(VARS::post("MEASLES_MONTH1")) : "",  //麻疹（月）
                             "MEASLES_YEAR2"            =>  VARS::post("MEASLES_YEAR2"),        //麻疹（年）
                             "MEASLES_MONTH2"           =>  (VARS::post("MEASLES_MONTH2")) ? intval(VARS::post("MEASLES_MONTH2")) : "",  //麻疹（月）
                             "MEASLES_YEAR3"            =>  VARS::post("MEASLES_YEAR3"),        //麻疹（年）
                             "MEASLES_MONTH3"           =>  (VARS::post("MEASLES_MONTH3")) ? intval(VARS::post("MEASLES_MONTH3")) : "",  //麻疹（月）
                             "VACCINE"                  =>  VARS::post("VACCINE"),              //ワクチンの種類
                             "LOT_NO"                   =>  VARS::post("LOT_NO"),               //ロット番号
                             "CONFIRMATION"             =>  VARS::post("CONFIRMATION"),         //麻疹（確認方法）
                             "A_MEASLES"                =>  VARS::post("A_MEASLES"),            //麻疹（罹患歴）
                             "A_MEASLES_AGE"            =>  (VARS::post("A_MEASLES_AGE") == "") ? "" : intval(VARS::post("A_MEASLES_AGE")),  //麻疹（罹患歴）年齢
                             "A_CONFIRMATION"           =>  VARS::post("A_CONFIRMATION"),       //麻疹（罹患歴）確認方法
                             "ANTIBODY"                 =>  VARS::post("ANTIBODY"),             //抗体検査
                             "ANTIBODY_YEAR"            =>  VARS::post("ANTIBODY_YEAR"),        //抗体検査（年）
                             "ANTIBODY_MONTH"           =>  (VARS::post("ANTIBODY_MONTH")) ? intval(VARS::post("ANTIBODY_MONTH")) : "",  //抗体検査（月）
                             "ANTIBODY_POSITIVE"        =>  VARS::post("ANTIBODY_POSITIVE"),    //抗体有無

                             "GRADE"                    =>  VARS::post("GRADE"),                //学年（年度）

                             "INSURED_NAME"             =>  VARS::post("INSURED_NAME"),         //被保険者氏名
                             "INSURED_MARK"             =>  VARS::post("INSURED_MARK"),         //加入者記号
                             "INSURED_NO"               =>  VARS::post("INSURED_NO"),           //加入者番号
                             "INSURANCE_NAME"           =>  VARS::post("INSURANCE_NAME"),       //保険者名称
                             "INSURANCE_NO"             =>  VARS::post("INSURANCE_NO"),         //保険者番号
                             "VALID_DATE"               =>  VARS::post("VALID_DATE"),           //有効期限
                             "AUTHORIZE_DATE"           =>  VARS::post("AUTHORIZE_DATE"),       //被扶養者認定年月
                             "INFIARMARY_REMARK"        =>  VARS::post("INFIARMARY_REMARK"),    //保健室記入用
                             "ATTENTION"                =>  VARS::post("ATTENTION"),            //注意点

                             "DBNAME"                   =>  VARS::post("DBNAME")                //DB変更に備えて
                            );

        $this->checked = VARS::request("CHECKED");     //チェック

        $this->cmd = VARS::request("cmd");

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
        if (VARS::get("SEQ") != "") {
            $this->seq = VARS::get("SEQ");
        }
        if (VARS::get("RELANO") != "") {
            $this->relano = VARS::get("RELANO");
        }

        //リストの年組コンボが変更されたとき
        if (VARS::get("init")) {
            unset($this->schregno);
            unset($this->name);
            unset($this->grade);
            unset($this->hr_class);
            unset($this->attendno);

            unset($this->seq);
            unset($this->relano);
        }

        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        //校種取得
        $db = Query::dbCheckOut();
        $this->schKind = $db->getOne(knjf160Query::getSchoolKind($this));

        //生徒項目名切替処理
        $schName = "";
        //テーブルの有無チェック
        $query = knjf160Query::checkTableExist();
        $table_cnt = $db->getOne($query);
        if ($table_cnt > 0 && ($this->schregno || $this->Properties["use_prg_schoolkind"] == "1")) {
            //生徒項目名取得
            $schName = $db->getOne(knjf160Query::getSchName($this));
        } elseif ($table_cnt > 0 && ($this->schregno || ($this->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != ""))) {
            //生徒項目名取得
            $schName = $db->getOne(knjf160Query::getSchName($this));
        }
        $this->sch_label = (strlen($schName) > 0) ? $schName : '生徒';
        Query::dbCheckIn($db);
    }

    //削除用のモデルを返します
    function &getDeleteModel()
    {
        $db = Query::dbCheckOut();

        if (!isset($this->schregno)) {
            $this->setWarning("MSG308");
            return;
        }

        $cmd = preg_split("/_/", $this->cmd);

        if($cmd[0] == "subform1" || $cmd[0] == "subform2"){
            $table = ($cmd[0] == "subform1") ? "HEALTH_BEF_SICKREC_DAT" : "HEALTH_AFT_SICKREC_DAT";
            knjf160Query::getDeleteQuery($db, $this, $table);
            $this->checked = "";
        } else if($cmd[0] == "subform3"){
            knjf160Query::getDeleteQuery3($db, $this);
            $this->relano = "";
        }

        $this->setMessage("MSG201");
        $this->cmd = $cmd[0]."A";
        $this->field = array();
        Query::dbCheckIn($db);

        return;
    }

    //入力項目チェック
    function validateInput($cmd)
    {
        if (!isset($this->schregno)) {
            $this->setWarning("MSG308");
            return false;
        }

        //入学前後の病気・けが
        if (strlen($this->field["DISEASE"]) > 60) {
            $this->setWarning("MSG915","(病名は60バイトまでです)");
            return false;
        }
        if (strlen($this->field["SITUATION"]) > 120) {
            $this->setWarning("MSG915","(経過は120バイトまでです)");
            return false;
        }
        if (strlen($this->field["HOSPITAL"]) > 60) {
            $this->setWarning("MSG915","(病院名は60バイトまでです)");
            return false;
        }
        if (strlen($this->field["DOCTOR"]) > 60) {
            $this->setWarning("MSG915","(主治医は60バイトまでです)");
            return false;
        }
        if (strlen($this->field["TELNO"]) > 14) {
            $this->setWarning("MSG915","(電話番号は14バイトまでです)");
            return false;
        }
        if (strlen($this->field["MEDICINE"]) > 60) {
            $this->setWarning("MSG915","(服用薬は60バイトまでです)");
            return false;
        }

        //家族情報
        if (strlen($this->field["RELANAME"]) > 60) {
            $this->setWarning("MSG915","(氏名は60バイトまでです)");
            return false;
        }
        if (strlen($this->field["RELAKANA"]) > 120) {
            $this->setWarning("MSG915","(氏名かなは120バイトまでです)");
            return false;
        }
        if (strlen($this->field["REMARK"]) > 90) {
            $this->setWarning("MSG915","(健康状態は90バイトまでです)");
            return false;
        }

        //その他情報
        if (strlen($this->field["ALLERGY_MEDICINE"]) > 120) {
            $this->setWarning("MSG915","(薬アレルギーは120バイトまでです)");
            return false;
        }
        if (strlen($this->field["ALLERGY_FOOD"]) > 120) {
            $this->setWarning("MSG915","(食物アレルギーは120バイトまでです)");
            return false;
        }
        if (strlen($this->field["ALLERGY_OTHER"]) > 120) {
            $this->setWarning("MSG915","(その他アレルギーは120バイトまでです)");
            return false;
        }
        if (strlen($this->field["HEART_DISEASE"]) > 45) {
            $this->setWarning("MSG915","(心臓疾患は45バイトまでです)");
            return false;
        }
        if (strlen($this->field["KIDNEY_DISEASE"]) > 45) {
            $this->setWarning("MSG915","(腎臓疾患は45バイトまでです)");
            return false;
        }
        if (strlen($this->field["OTHER_DISEASE"]) > 120) {
            $this->setWarning("MSG915","(その他は120バイトまでです)");
            return false;
        }

        //健康調査
        if ($cmd == "subform5" && (strlen($this->field["ATTENTION"]) > 600)) {
            $this->setWarning("MSG915","(注意すべき点は600バイトまでです)");
            return false;
        }

        //保健室記入
        if (strlen($this->field["INSURED_NAME"]) > 60) {
            $this->setWarning("MSG915","(被保険者氏名は60バイトまでです)");
            return false;
        }
        if (strlen($this->field["INSURED_MARK"]) > 60) {
            $this->setWarning("MSG915","(加入者記号は60バイトまでです)");
            return false;
        }
        if (strlen($this->field["INSURED_NO"]) > 20) {
            $this->setWarning("MSG915","(加入者番号は20バイトまでです)");
            return false;
        }
        if (strlen($this->field["INSURANCE_NAME"]) > 60) {
            $this->setWarning("MSG915","(保険者名称は60バイトまでです)");
            return false;
        }
        if (strlen($this->field["INSURANCE_NO"]) > 20) {
            $this->setWarning("MSG915","(保険者番号は20バイトまでです)");
            return false;
        }
        if (strlen($this->field["INFIARMARY_REMARK"]) > 1200) {
            $this->setWarning("MSG915","(保健室記入用は1200バイトまでです)");
            return false;
        }
        if ($cmd == "subform6" && (strlen($this->field["ATTENTION"]) > 90)) {
            $this->setWarning("MSG915","(注意点は90バイトまでです)");
            return false;
        }

        return true;
    }

    //更新用のモデルを返します
    function &getUpdateModel()
    {
        $db = Query::dbCheckOut();

        $cmd = preg_split("/_/", $this->cmd);

        if (!$this->validateInput($cmd[0])) {
            $this->cmd = $cmd[0]."A";
            return;
        }

        if($cmd[0] == "subform1" || $cmd[0] == "subform2"){
            $table = ($cmd[0] == "subform1") ? "HEALTH_BEF_SICKREC_DAT" : "HEALTH_AFT_SICKREC_DAT";
            knjf160Query::getUpdateQuery($db, $this, $this->field, $cmd[1], $table);
            $this->seq = ($cmd[1] == "insert") ? $db->getOne(knjf160Query::getMaxSeq($db, $this, $table)) : $this->seq;
        } else if($cmd[0] == "subform3"){
            knjf160Query::getUpdateQuery3($db, $this, $this->field, $cmd[1]);
            $this->relano = ($cmd[1] == "insert") ? $db->getOne(knjf160Query::getRelano($this, "schreg", "max")) : $this->relano;
        } else if($cmd[0] == "subform4"){
            knjf160Query::getUpdateQuery4($db, $this, $this->field);
        } else if($cmd[0] == "subform5"){
            //初期化
            $this->fields = array();
            for ($i=0; $i < get_count($this->data["QUESTIONCD"]); $i++) {

                if(get_count($this->data["YEAR"]) > 0){

                    foreach ($this->data["YEAR"] as $code => $col) {
                        $post_name = VARS::post("ANSWER".$code."-".$i);
                        if (isset($post_name)) $this->fields["ANSWER".$code][$i] = $post_name;

                        $post_name2 = VARS::post("GRADE".$code);
                        if (isset($post_name2)) $this->fields["GRADE".$code][$i] = $post_name2;

                        $post_name3 = VARS::post("E_YEAR".$code);
                        if (isset($post_name3)) $this->fields["E_YEAR".$code][$i] = $post_name3;

                        $post_name4 = VARS::post("E_MONTH".$code);
                        if (isset($post_name4)) $this->fields["E_MONTH".$code][$i] = $post_name4;
                    }
                }
                $code2 = (int)$code+1;
                $this->fields["ANSWER".$code2][$i] = VARS::post("ANSWER".$code2."-".$i);
            }

            knjf160Query::getUpdateQuery5($db, $this);
            $this->field = array();

        } else if($cmd[0] == "subform6"){
            knjf160Query::getUpdateQuery6($db, $this, $this->field);
        }

        $this->cmd = $cmd[0]."A";
        $this->setMessage("MSG201");
        Query::dbCheckIn($db);

        return;
    }
}
?>
