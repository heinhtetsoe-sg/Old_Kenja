<?php

require_once('for_php7.php');
class knjf010Model extends Model {

    var $cmd;
    var $schregno;          //生徒学籍番号
    var $name;              //生徒名
    var $GradeClass;        //学年、クラス
    var $Hrname;            //クラス名称
    var $year;              //現在処理年度
    var $semester;          //現在処理学期
    var $staffcd;           //職員コード
    var $field = array();           //フィールドデータ
    var $control_data = array();    //コントロールマスタデータ
    var $replace_data = array();    //一括更新処理データ

    function knjf010Model()
    {
        common::GetControlMaster_Fnc($this->control_data);
    }

    function init()
    {
        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->staffcd = STAFFCD;

        if (VARS::get("PROGRAMID")) {
            unset($this->schregno);
            unset($this->name);
            unset($this->GradeClass);
            unset($this->Hrname);
        }
        //リストの年組コンボが変更されたとき
        if (VARS::get("init")) {
            unset($this->schregno);
            unset($this->name);
            unset($this->GradeClass);
            unset($this->Hrname);
        }
        if (VARS::get("SCHREGNO")){
            $this->schregno  = VARS::get("SCHREGNO");
        }
        if (VARS::get("NAME")) {
            $this->name = VARS::get("NAME");
        }
        //フィールド
        $this->field = array("DATE"                  =>  VARS::post("DATE"),                 //健康診断実施日付
                             "HEIGHT"                =>  VARS::post("HEIGHT"),               //身長
                             "WEIGHT"                =>  VARS::post("WEIGHT"),               //体重
                             "SITHEIGHT"             =>  VARS::post("SITHEIGHT"),            //座高
                             "R_BAREVISION"          =>  VARS::post("R_BAREVISION"),         //視力・右裸眼（数字）
                             "R_BAREVISION_MARK"     =>  VARS::post("R_BAREVISION_MARK"),    //視力・右裸眼（文字）
                             "R_VISION"              =>  VARS::post("R_VISION"),             //視力・右矯正（数字）
                             "R_VISION_MARK"         =>  VARS::post("R_VISION_MARK"),        //視力・右矯正（文字）
                             "L_BAREVISION"          =>  VARS::post("L_BAREVISION"),         //視力・左裸眼（数字）
                             "L_BAREVISION_MARK"     =>  VARS::post("L_BAREVISION_MARK"),    //視力・左裸眼（文字）
                             "L_VISION"              =>  VARS::post("L_VISION"),             //視力・左矯正（数字）
                             "L_VISION_MARK"         =>  VARS::post("L_VISION_MARK"),        //視力・左矯正（文字）
                             "EYE_TEST_RESULT"       =>  VARS::post("EYE_TEST_RESULT"),      //眼科検診結果
                             "R_EAR_DB"              =>  VARS::post("R_EAR_DB"),             //聴力・右DB
                             "R_EAR"                 =>  VARS::post("R_EAR"),                //聴力・右状態
                             "L_EAR_DB"              =>  VARS::post("L_EAR_DB"),             //聴力・左DB
                             "L_EAR"                 =>  VARS::post("L_EAR"),                //聴力・左状態
                             "ALBUMINURIA1CD"        =>  VARS::post("ALBUMINURIA1CD"),       //尿・１次蛋白
                             "URICSUGAR1CD"          =>  VARS::post("URICSUGAR1CD"),         //尿・１次糖
                             "URICBLEED1CD"          =>  VARS::post("URICBLEED1CD"),         //尿・１次潜血
                             "ALBUMINURIA2CD"        =>  VARS::post("ALBUMINURIA2CD"),       //尿・２次蛋白
                             "URICSUGAR2CD"          =>  VARS::post("URICSUGAR2CD"),         //尿・２次糖
                             "URICBLEED2CD"          =>  VARS::post("URICBLEED2CD"),         //尿・２次潜血
                             "URICOTHERTEST"         =>  VARS::post("URICOTHERTEST"),        //尿・その他の検査
                             "NUTRITIONCD"           =>  VARS::post("NUTRITIONCD"),          //栄養状態
                             "SPINERIBCD"            =>  VARS::post("SPINERIBCD"),           //脊柱・胸部
                             "EYEDISEASECD"          =>  VARS::post("EYEDISEASECD"),         //目の疾病及び異常
                             "NOSEDISEASECD"         =>  VARS::post("NOSEDISEASECD"),        //耳鼻咽頭疾患
                             "SKINDISEASECD"         =>  VARS::post("SKINDISEASECD"),        //皮膚疾患
                             "HEART_MEDEXAM"         =>  VARS::post("HEART_MEDEXAM"),        //心臓・臨床医学的検査
                             "HEART_MEDEXAM_REMARK"  =>  VARS::post("HEART_MEDEXAM_REMARK"), //心臓・臨床医学的検査---NO005
                             "HEARTDISEASECD"        =>  VARS::post("HEARTDISEASECD"),       //心臓・疾病及び異常
                             "TB_FILMDATE"           =>  VARS::post("TB_FILMDATE"),          //結核・撮影日付
                             "TB_FILMNO"             =>  VARS::post("TB_FILMNO"),            //結核・フィルム番号
                             "TB_REMARKCD"           =>  VARS::post("TB_REMARKCD"),          //結核・所見
                             "TB_OTHERTESTCD"        =>  VARS::post("TB_OTHERTESTCD"),       //結核・その他検査
                             "TB_NAMECD"             =>  VARS::post("TB_NAMECD"),            //結核・病名
                             "TB_ADVISECD"           =>  VARS::post("TB_ADVISECD"),          //結核・指導区分
                             "TB_X_RAY"              =>  VARS::post("TB_X_RAY"),             //結核・検査(X線)
                             "ANEMIA_REMARK"         =>  VARS::post("ANEMIA_REMARK"),        //貧血・所見
                             "HEMOGLOBIN"            =>  VARS::post("HEMOGLOBIN"),           //貧血・ヘモグロビン値
                             "OTHERDISEASECD"        =>  VARS::post("OTHERDISEASECD"),       //その他疾病及び異常
                             "DOC_REMARK"            =>  VARS::post("DOC_REMARK"),           //学校医・所見
                             "DOC_DATE"              =>  VARS::post("DOC_DATE"),             //学校医・所見日付
                             "TREATCD"               =>  VARS::post("TREATCD"),              //事後処置
                             "REMARK"                =>  VARS::post("REMARK"),               //備考
                             "GUIDE_DIV"             =>  VARS::post("GUIDE_DIV"),            //運動/指導区分
                             "JOINING_SPORTS_CLUB"   =>  VARS::post("JOINING_SPORTS_CLUB"),  //運動/部活動
                             "MEDICAL_HISTORY"       =>  VARS::post("MEDICAL_HISTORY")       //既往症
                            );
        $this->year = CTRL_YEAR;
        $this->semester= CTRL_SEMESTER;
        //一括更新処理処理1
        if ($this->cmd == "replace_update1") {
            $this->replace_data["selectdata"]  = VARS::post("selectdata");
            $this->replace_data["head_field"]  = $this->replace_data["det_field"] = array();
            $this->replace_data["head_check"]  = array(VARS::post("RCHECK0"));
            $this->replace_data["det_check"]   = array(VARS::post("RCHECK1"),
                                                       VARS::post("RCHECK2"),
                                                       VARS::post("RCHECK3"),
                                                       VARS::post("RCHECK4"),
                                                       VARS::post("RCHECK5"),
                                                       VARS::post("RCHECK6"),
                                                       VARS::post("RCHECK7"),
                                                       VARS::post("RCHECK8"));
            $this->replace_data["check_all"] = VARS::post("RCHECK9");
            $this->replace_data["head_field"]["DATE"] = "";
            if (VARS::post("RCHECK0")=="1") $this->replace_data["head_field"]["DATE"]              = VARS::post("DATE");
            if (VARS::post("RCHECK1")=="1") $this->replace_data["det_field"]["R_BAREVISION"]       = VARS::post("R_BAREVISION");
            if (VARS::post("RCHECK1")=="1") $this->replace_data["det_field"]["R_BAREVISION_MARK"]  = VARS::post("R_BAREVISION_MARK");
            if (VARS::post("RCHECK2")=="1") $this->replace_data["det_field"]["R_VISION"]           = VARS::post("R_VISION");
            if (VARS::post("RCHECK2")=="1") $this->replace_data["det_field"]["R_VISION_MARK"]      = VARS::post("R_VISION_MARK");
            if (VARS::post("RCHECK3")=="1") $this->replace_data["det_field"]["L_BAREVISION"]       = VARS::post("L_BAREVISION");
            if (VARS::post("RCHECK3")=="1") $this->replace_data["det_field"]["L_BAREVISION_MARK"]  = VARS::post("L_BAREVISION_MARK");
            if (VARS::post("RCHECK4")=="1") $this->replace_data["det_field"]["L_VISION"]           = VARS::post("L_VISION");
            if (VARS::post("RCHECK4")=="1") $this->replace_data["det_field"]["L_VISION_MARK"]      = VARS::post("L_VISION_MARK");
            if (VARS::post("RCHECK5")=="1") $this->replace_data["det_field"]["R_EAR_DB"]           = VARS::post("R_EAR_DB");
            if (VARS::post("RCHECK6")=="1") $this->replace_data["det_field"]["R_EAR"]              = VARS::post("R_EAR");
            if (VARS::post("RCHECK7")=="1") $this->replace_data["det_field"]["L_EAR_DB"]           = VARS::post("L_EAR_DB");
            if (VARS::post("RCHECK8")=="1") $this->replace_data["det_field"]["L_EAR"]              = VARS::post("L_EAR");
        }
        //一括更新処理処理2
        if ($this->cmd == "replace_update2") {
            $this->replace_data["selectdata"]  = VARS::post("selectdata");
            $this->replace_data["head_field"]  = $this->replace_data["det_field"] = array();
            $this->replace_data["head_check"]  = array(VARS::post("RCHECK0"));
            $this->replace_data["det_check"]   = array(VARS::post("RCHECK1"),
                                                       VARS::post("RCHECK2"),
                                                       VARS::post("RCHECK3"),
                                                       VARS::post("RCHECK4"),
                                                       VARS::post("RCHECK5"),
                                                       VARS::post("RCHECK6"),
                                                       VARS::post("RCHECK7"),
                                                       VARS::post("RCHECK8"),
                                                       VARS::post("RCHECK9"),
                                                       VARS::post("RCHECK10"),
                                                       VARS::post("RCHECK11"),
                                                       VARS::post("RCHECK12"),
                                                       VARS::post("RCHECK13"),
                                                       VARS::post("RCHECK14"),
                                                       VARS::post("RCHECK15"));
            $this->replace_data["check_all"] = VARS::post("RCHECK16");
            $this->replace_data["head_field"]["DATE"] = "";
            if (VARS::post("RCHECK1")=="1")  $this->replace_data["det_field"]["ALBUMINURIA1CD"]     = VARS::post("ALBUMINURIA1CD");
            if (VARS::post("RCHECK2")=="1")  $this->replace_data["det_field"]["URICSUGAR1CD"]       = VARS::post("URICSUGAR1CD");
            if (VARS::post("RCHECK3")=="1")  $this->replace_data["det_field"]["URICBLEED1CD"]       = VARS::post("URICBLEED1CD");
            if (VARS::post("RCHECK4")=="1")  $this->replace_data["det_field"]["ALBUMINURIA2CD"]     = VARS::post("ALBUMINURIA2CD");
            if (VARS::post("RCHECK5")=="1")  $this->replace_data["det_field"]["URICSUGAR2CD"]       = VARS::post("URICSUGAR2CD");
            if (VARS::post("RCHECK6")=="1")  $this->replace_data["det_field"]["URICBLEED2CD"]       = VARS::post("URICBLEED2CD");
            if (VARS::post("RCHECK7")=="1")  $this->replace_data["det_field"]["URICOTHERTEST"]      = VARS::post("URICOTHERTEST");
            if (VARS::post("RCHECK8")=="1")  $this->replace_data["det_field"]["NUTRITIONCD"]        = VARS::post("NUTRITIONCD");
            if (VARS::post("RCHECK9")=="1")  $this->replace_data["det_field"]["SPINERIBCD"]         = VARS::post("SPINERIBCD");
            if (VARS::post("RCHECK10")=="1") $this->replace_data["det_field"]["EYEDISEASECD"]       = VARS::post("EYEDISEASECD");
            if (VARS::post("RCHECK11")=="1") $this->replace_data["det_field"]["EYE_TEST_RESULT"]    = VARS::post("EYE_TEST_RESULT");
            if (VARS::post("RCHECK12")=="1") $this->replace_data["det_field"]["NOSEDISEASECD"]      = VARS::post("NOSEDISEASECD");
            if (VARS::post("RCHECK13")=="1") $this->replace_data["det_field"]["SKINDISEASECD"]      = VARS::post("SKINDISEASECD");
            if (VARS::post("RCHECK14")=="1") $this->replace_data["det_field"]["HEART_MEDEXAM"]      = VARS::post("HEART_MEDEXAM");
            if (VARS::post("RCHECK15")=="1") $this->replace_data["det_field"]["HEARTDISEASECD"]     = VARS::post("HEARTDISEASECD");
        }
        //一括更新処理処理3
        if ($this->cmd == "replace_update3") {
            $this->replace_data["selectdata"]  = VARS::post("selectdata");
            $this->replace_data["head_field"]  = $this->replace_data["det_field"] = array();
            $this->replace_data["head_check"]  = array(VARS::post("RCHECK0"));
            $this->replace_data["det_check"]   = array(VARS::post("RCHECK1"),
                                                       VARS::post("RCHECK2"),
                                                       VARS::post("RCHECK3"),
                                                       VARS::post("RCHECK4"),
                                                       VARS::post("RCHECK5"),
                                                       VARS::post("RCHECK6"),
                                                       VARS::post("RCHECK7"),
                                                       VARS::post("RCHECK8"),
                                                       VARS::post("RCHECK9"),
                                                       VARS::post("RCHECK10"),
                                                       VARS::post("RCHECK11"),
                                                       VARS::post("RCHECK12"),
                                                       VARS::post("RCHECK13"));
            $this->replace_data["check_all"] = VARS::post("RCHECK14");
            $this->replace_data["head_field"]["DATE"] = "";
            if (VARS::post("RCHECK1")=="1"){
                $this->replace_data["det_field"]["TB_FILMDATE"] = VARS::post("TB_FILMDATE");
                $this->replace_data["det_field"]["TB_FILMDATE"] = str_replace("/","-",$this->replace_data["det_field"]["TB_FILMDATE"]);
            }
            if (VARS::post("RCHECK2")=="1")  $this->replace_data["det_field"]["TB_REMARKCD"]    = VARS::post("TB_REMARKCD");
            if (VARS::post("RCHECK3")=="1")  $this->replace_data["det_field"]["TB_X_RAY"]       = VARS::post("TB_X_RAY");
            if (VARS::post("RCHECK4")=="1")  $this->replace_data["det_field"]["TB_OTHERTESTCD"] = VARS::post("TB_OTHERTESTCD");
            if (VARS::post("RCHECK5")=="1")  $this->replace_data["det_field"]["TB_NAMECD"]      = VARS::post("TB_NAMECD");
            if (VARS::post("RCHECK6")=="1")  $this->replace_data["det_field"]["TB_ADVISECD"]    = VARS::post("TB_ADVISECD");
            if (VARS::post("RCHECK7")=="1")  $this->replace_data["det_field"]["ANEMIA_REMARK"]  = VARS::post("ANEMIA_REMARK");
            if (VARS::post("RCHECK8")=="1")  $this->replace_data["det_field"]["HEMOGLOBIN"]     = VARS::post("HEMOGLOBIN");
            if (VARS::post("RCHECK9")=="1")  $this->replace_data["det_field"]["OTHERDISEASECD"] = VARS::post("OTHERDISEASECD");
            if (VARS::post("RCHECK10")=="1") $this->replace_data["det_field"]["DOC_REMARK"]     = VARS::post("DOC_REMARK");
            if (VARS::post("RCHECK11")=="1"){
                $this->replace_data["det_field"]["DOC_DATE"] = VARS::post("DOC_DATE");
                $this->replace_data["det_field"]["DOC_DATE"] = str_replace("/","-",$this->replace_data["det_field"]["DOC_DATE"]);
            }
            if (VARS::post("RCHECK12")=="1") $this->replace_data["det_field"]["TREATCD"]        = VARS::post("TREATCD");
            if (VARS::post("RCHECK13")=="1") $this->replace_data["det_field"]["REMARK"]         = VARS::post("REMARK");
        }
        //一括更新処理処理4
        if ($this->cmd == "replace_update4") {
            $this->replace_data["selectdata"]  = VARS::post("selectdata");
            $this->replace_data["head_field"]  = $this->replace_data["det_field"] = array();
            $this->replace_data["head_check"]  = array(VARS::post("RCHECK0"));
            $this->replace_data["det_check"]   = array(VARS::post("RCHECK1"),
                                                       VARS::post("RCHECK2"),
                                                       VARS::post("RCHECK3"));
            $this->replace_data["check_all"] = VARS::post("RCHECK4");
            $this->replace_data["head_field"]["DATE"] = "";
            if (VARS::post("RCHECK1")=="1")  $this->replace_data["det_field"]["GUIDE_DIV"]              = VARS::post("GUIDE_DIV");
            if (VARS::post("RCHECK2")=="1")  $this->replace_data["det_field"]["JOINING_SPORTS_CLUB"]    = VARS::post("JOINING_SPORTS_CLUB");
            if (VARS::post("RCHECK3")=="1")  $this->replace_data["det_field"]["MEDICAL_HISTORY"]        = VARS::post("MEDICAL_HISTORY");
        }
        if(VARS::get("ini2")) {
            unset($this->replace_data);
        }
    }
    //入力項目チェック
    function validateInput()
    {
        if ($this->schregno=="") {
            $this->setWarning("MSG308");
            return false;
        }
        //健康診断実施日が入力されていない場合現在処理日付(ATTEND_CTRL_DATE)を代入
        if($this->field["DATE"]==""){
            if(VARS::post("HIDDENDATE")){
                $this->field["DATE"] = VARS::post("HIDDENDATE");
            }else{
                $this->field["DATE"] = CTRL_DATE;
            }
        }
        $height = explode(".",$this->field["HEIGHT"]);
        $weight = explode(".",$this->field["WEIGHT"]);
        $sitheight = explode(".",$this->field["SITHEIGHT"]);
        $hemoglobin = explode(".",$this->field["HEMOGLOBIN"]);
        if(strlen($height[0])>3){
            $this->setWarning("MSG901","(身長)");
            return false;
        }elseif(strlen($height[1])>1){
            $this->setWarning("MSG901","(身長) 小数点第一位まで");
            return false;
        }
        if(strlen($weight[0])>3){
            $this->setWarning("MSG901","(体重)");
            return false;
        }elseif(strlen($weight[1])>1){
            $this->setWarning("MSG901","(体重) 小数点第一位まで");
            return false;
        }
        if(strlen($sitheight[0])>3){
            $this->setWarning("MSG901","(座高)");
            return false;
        }elseif(strlen($sitheight[1])>1){
            $this->setWarning("MSG901","(座高) 小数点第一位まで");
            return false;
        }

        $r_barevision = explode(".",$this->field["R_BAREVISION"]);
        $r_vision = explode(".",$this->field["R_VISION"]);
        $l_barevision = explode(".",$this->field["L_BAREVISION"]);
        $l_vision = explode(".",$this->field["L_VISION"]);
        if(strlen($r_barevision[0])>1){
            $this->setWarning("MSG901","(視力・右裸眼)");
            return false;
        }elseif(strlen($r_barevision[1])>2){
            $this->setWarning("MSG901","(視力・右裸眼) 小数点第２位まで");
            return false;
        }
        if(strlen($r_vision[0])>1){
            $this->setWarning("MSG901","(視力・右矯正)");
            return false;
        }elseif(strlen($r_vision[1])>2){
            $this->setWarning("MSG901","(視力・右矯正) 小数点第２位まで");
            return false;
        }
        if(strlen($l_barevision[0])>1){
            $this->setWarning("MSG901","(視力・左裸眼)");
            return false;
        }elseif(strlen($l_barevision[1])>2){
            $this->setWarning("MSG901","(視力・左裸眼) 小数点第２位まで");
            return false;
        }
        if(strlen($l_vision[0])>1){
            $this->setWarning("MSG901","(視力・左矯正)");
            return false;
        }elseif(strlen($l_vision[1])>2){
            $this->setWarning("MSG901","(視力・左矯正) 小数点第２位まで");
            return false;
        }

        if ($this->field["R_EAR_DB"] > 999) {
            $this->setWarning("MSG915","聴力・右DBは半角数字で999までです。");
            return false;
        }
        if ($this->field["L_EAR_DB"] > 999) {
            $this->setWarning("MSG915","聴力・右DBは半角数字で999までです。");
            return false;
        }
        if (strlen($this->field["URICOTHERTEST"]) > 60) {
            $this->setWarning("MSG915","尿・その他の検査は60バイトまでです。");
            return false;
        }
        if (strlen($this->replace_data["det_field"]["EYE_TEST_RESULT"]) > 60) {
            $this->setWarning("MSG915","眼科検診結果は60バイトまでです。");
            return false;
        }
        if (strlen($this->field["HEART_MEDEXAM_REMARK"]) > 120) {
            $this->setWarning("MSG915","心臓臨床医学的検査は120バイトまでです。");
            return false;
        }
        if (strlen($this->field["TB_FILMNO"]) > 6) {
            $this->setWarning("MSG915","結核・フィルム番号は6バイトまでです。");
            return false;
        }
        if (strlen($this->field["TB_X_RAY"]) > 60) {
            $this->setWarning("MSG915","結核・検査(X線)は60バイトまでです。");
            return false;
        }
        if (strlen($this->field["ANEMIA_REMARK"]) > 30) {
            $this->setWarning("MSG915","貧血・所見は30バイトまでです。");
            return false;
        }
        if(strlen($hemoglobin[0])>2){
            $this->setWarning("MSG901","ヘモグロビン値");
            return false;
        }elseif(strlen($hemoglobin[1])>1){
            $this->setWarning("MSG901","ヘモグロビン値は 小数第１位まで");
            return false;
        }
        if (strlen($this->field["DOC_REMARK"]) > 30) {
            $this->setWarning("MSG915","学校医・所見は30バイトまでです。");
            return false;
        }
        if (strlen($this->field["REMARK"]) > 30) {
            $this->setWarning("MSG915","備考は30バイトまでです。");
            return false;
        }
        $this->field["DATE"] = str_replace("/","-",$this->field["DATE"]);
        $this->field["TB_FILMDATE"] = str_replace("/","-",$this->field["TB_FILMDATE"]);
        $this->field["DOC_DATE"] = str_replace("/","-",$this->field["DOC_DATE"]);

        if (strlen($r_barevision[0]) > 0) 
            $this->field["R_BAREVISION"] = $r_barevision[0] . "." . sprintf("%-02s", $r_barevision[1]);
        if (strlen($r_vision[0]) > 0) 
            $this->field["R_VISION"] = $r_vision[0] . "." . sprintf("%-02s", $r_vision[1]);
        if (strlen($l_barevision[0]) > 0) 
            $this->field["L_BAREVISION"] = $l_barevision[0] . "." . sprintf("%-02s", $l_barevision[1]);
        if (strlen($l_vision[0]) > 0) 
            $this->field["L_VISION"] = $l_vision[0] . "." . sprintf("%-02s", $l_vision[1]);

        return true;
    }

    //更新処理
    function &getUpdateModel()
    {
        if (!$this->validateInput()) {
            return;
        }

        $this->checkVisionMark($this->field);//視力の文字をチェック
        $this->update();
        $this->setMessage("MSG201");
    }

    function checkVisionMark($fieldArray) {
        $visions = array('R_BAREVISION', 'R_VISION', 'L_BAREVISION', 'L_VISION');
        foreach ($visions as $val) {
            $valMark = $val . "_MARK";
            if (!$fieldArray[$valMark] && $fieldArray[$val] != '') {
                $query = knjf010Query::getVisionMark();
                $db = Query::dbCheckOut();
                $result = $db->query($query);
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    if ($fieldArray[$val] >= $row["NAMESPARE1"]){
                        $fieldArray[$valMark] = $row["NAME1"];
                    } else {
                        break 1;
                    }
                }
                Query::dbCheckIn($db);
            }
        }
    }

    function &update()
    {
        //レコードが存在するのかチェック
        $db = Query::dbCheckOut();
        $query = knjf010Query::checkHdat($this);
        $count = $db->getOne($query);
        Query::dbCheckIn($db);
        if ($count >= 1) {
            knjf010Query::getHdat_update($this);//あればMEDEXAM_HDATをアップデート
        } else {
            knjf010Query::getHdat_insert($this);//なければMEDEXAM_HDATにインサート
        }
        //年度及び学籍番号でMEDEXAM_DET_DATが存在する場合は更新(UPDATE)、存在しない場合(INSERT)する
        $row = knjf010Query::getMedexam_det_dat($this);       //MEDEXAM_DET_DAT取得
        //データが存在しない場合
        if($row == ""){
            $flag = "insert";
        }
        knjf010Query::getInsertUpdateQuery($this,$flag);
        return $result;
    }

    //削除処理
    function &getDeleteModel()
    {
        if ($this->schregno=="") {
            $this->setWarning("MSG308");
            return false;
        }
        //年度及び学籍番号でMEDEXAM_DET_DATが存在する場合、削除(DELETE)
        $row = knjf010Query::getMedexam_det_dat($this);       //MEDEXAM_DET_DAT取得
        //データが存在しない場合エラー
        if($row == ""){
            $this->setWarning("MSG303");
            return false;
        }
        if($this->delete()){
            $this->setMessage("MSG201");
            return;
        }
    }

    function &delete()
    {
        knjf010Query::getDeleteQuery($this);
        $row = knjf010Query::getMedexam_tooth_dat($this);

        if($row == "") { //MEDEXAM_TOOTH_DATが存在しない場合はMEDEXAM_HDATを削除
            knjf010Query::getHdat_DeleteQuery($this);
        } else { //TOOTH_DATがあればMEDEXAM_HDATのDATEだけNULLにする
            knjf010Query::getHdatDate_DeleteQuery($this);
        }
        return true;
    }

    //一括更新処理1
    function &ReplaceModel1()
    {
        //生徒が選択されてない場合
        if(!strlen($this->replace_data["selectdata"])) {
            $this->setWarning("MSG304");
            return false;
        }
        $r_barevision = explode(".",$this->replace_data["det_field"]["R_BAREVISION"]);
        $r_vision = explode(".",$this->replace_data["det_field"]["R_VISION"]);
        $l_barevision = explode(".",$this->replace_data["det_field"]["L_BAREVISION"]);
        $l_vision = explode(".",$this->replace_data["det_field"]["L_VISION"]);
        if(strlen($r_barevision[0])>1){
            $this->setWarning("MSG901","(視力・右裸眼)");
            return false;
        }elseif(strlen($r_barevision[1])>2){
            $this->setWarning("MSG901","(視力・右裸眼) 小数点第２位まで");
            return false;
        }
        if(strlen($r_vision[0])>1){
            $this->setWarning("MSG901","(視力・右矯正)");
            return false;
        }elseif(strlen($r_vision[1])>2){
            $this->setWarning("MSG901","(視力・右矯正) 小数点第２位まで");
            return false;
        }
        if(strlen($l_barevision[0])>1){
            $this->setWarning("MSG901","(視力・左裸眼)");
            return false;
        }elseif(strlen($l_barevision[1])>2){
            $this->setWarning("MSG901","(視力・左裸眼) 小数点第２位まで");
            return false;
        }
        if(strlen($l_vision[0])>1){
            $this->setWarning("MSG901","(視力・左矯正)");
            return false;
        }elseif(strlen($l_vision[1])>2){
            $this->setWarning("MSG901","(視力・左矯正) 小数点第２位まで");
            return false;
        }

        if ($this->replace_data["det_field"]["R_EAR_DB"] > 999) {
            $this->setWarning("MSG915","聴力・右DBは半角数字で999までです。");
            return false;
        }
        if ($this->replace_data["det_field"]["L_EAR_DB"] > 999) {
            $this->setWarning("MSG915","聴力・右DBは半角数字で999までです。");
            return false;
        }
        $this->replace_data["head_field"]["DATE"] = str_replace("/","-",$this->replace_data["head_field"]["DATE"]);
        if (strlen($r_barevision[0]) > 0) 
            $this->replace_data["det_field"]["R_BAREVISION"] = $r_barevision[0] . "." . sprintf("%-02s", $r_barevision[1]);
        if (strlen($r_vision[0]) > 0) 
            $this->replace_data["det_field"]["R_VISION"] = $r_vision[0] . "." . sprintf("%-02s", $r_vision[1]);
        if (strlen($l_barevision[0]) > 0) 
            $this->replace_data["det_field"]["L_BAREVISION"] = $l_barevision[0] . "." . sprintf("%-02s", $l_barevision[1]);
        if (strlen($l_vision[0]) > 0) 
            $this->replace_data["det_field"]["L_VISION"] = $l_vision[0] . "." . sprintf("%-02s", $l_vision[1]);
        //最低ひとつでもチェックがついてる場合のみ更新
        if(!in_array("1",$this->replace_data["head_check"]) && !in_array("1",$this->replace_data["det_check"])) {
            return false;
        }
        $this->checkVisionMark($this->replace_data["det_field"]);//視力の文字をチェック
        if(in_array("1",$this->replace_data["det_check"]) || in_array("1",$this->replace_data["head_check"])){
            $this->updateHead();
            $this->updateDet();
        }
        $this->setMessage("MSG201");
        //添え字を０から順序良くする
        $this->replace_data["check"] = array_merge((array)$this->replace_data["head_check"],(array)$this->replace_data["det_check"]);
        $this->replace_data["check"][] = $this->replace_data["check_all"];
    }
    //一括更新処理2
    function &ReplaceModel2()
    {
        //生徒が選択されてない場合
        if(!strlen($this->replace_data["selectdata"])) {
            $this->setWarning("MSG304");
            return false;
        }
        if (strlen($this->replace_data["det_field"]["URICOTHERTEST"]) > 60) {
            $this->setWarning("MSG915","尿・その他の検査は60バイトまでです。");
            return false;
        }
        if (strlen($this->replace_data["det_field"]["EYE_TEST_RESULT"]) > 60) {
            $this->setWarning("MSG915","眼科検診結果は60バイトまでです。");
            return false;
        }
        //最低ひとつでもチェックがついてる場合のみ更新
        if(!in_array("1",$this->replace_data["det_check"])) {
            return false;
        }
        if(in_array("1",$this->replace_data["det_check"])){
            $this->updateHead();
            $this->updateDet();
        }
        $this->setMessage("MSG201");
        //添え字を０から順序良くする
        $this->replace_data["check"] = array_merge((array)$this->replace_data["head_check"],(array)$this->replace_data["det_check"]);
        $this->replace_data["check"][] = $this->replace_data["check_all"];
    }
    //一括更新処理3
    function &ReplaceModel3()
    {
        //生徒が選択されてない場合
        if(!strlen($this->replace_data["selectdata"])) {
            $this->setWarning("MSG304");
            return false;
        }
        $hemoglobin = explode(".",$this->replace_data["det_field"]["HEMOGLOBIN"]);
        if (strlen($this->replace_data["det_field"]["ANEMIA_REMARK"]) > 30) {
            $this->setWarning("MSG915","貧血・所見は30バイトまでです。");
            return false;
        }
        if(strlen($hemoglobin[0])>2){
            $this->setWarning("MSG901","ヘモグロビン値");
            return false;
        }elseif(strlen($hemoglobin[1])>1){
            $this->setWarning("MSG901","ヘモグロビン値は 小数第１位まで");
            return false;
        }
        if (strlen($this->replace_data["det_field"]["DOC_REMARK"]) > 30) {
            $this->setWarning("MSG915","学校医・所見は30バイトまでです。");
            return false;
        }
        if (strlen($this->replace_data["det_field"]["REMARK"]) > 30) {
            $this->setWarning("MSG915","備考は30バイトまでです。");
            return false;
        }
        //最低ひとつでもチェックがついてる場合のみ更新
        if(!in_array("1",$this->replace_data["det_check"])) {
            return false;
        }
        if(in_array("1",$this->replace_data["det_check"])){
            $this->updateHead();
            $this->updateDet();
        }
        $this->setMessage("MSG201");
        //添え字を０から順序良くする
        $this->replace_data["check"] = array_merge((array)$this->replace_data["head_check"],(array)$this->replace_data["det_check"]);
        $this->replace_data["check"][] = $this->replace_data["check_all"];
    }
    //一括更新処理4
    function &ReplaceModel4()
    {
        //生徒が選択されてない場合
        if(!strlen($this->replace_data["selectdata"])) {
            $this->setWarning("MSG304");
            return false;
        }
        //最低ひとつでもチェックがついてる場合のみ更新
        if(!in_array("1",$this->replace_data["det_check"])) {
            return false;
        }
        if(in_array("1",$this->replace_data["det_check"])){
            $this->updateHead();
            $this->updateDet();
        }
        $this->setMessage("MSG201");
        //添え字を０から順序良くする
        $this->replace_data["check"] = array_merge((array)$this->replace_data["head_check"],(array)$this->replace_data["det_check"]);
        $this->replace_data["check"][] = $this->replace_data["check_all"];
    }
    //MEDEXAM_HDATの更新
    function &updateHead()
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        $array = explode(",", $this->replace_data["selectdata"]);
        //健康診断実施日が入力されていない場合現在処理日付(ATTEND_CTRL_DATE)を代入
        if($this->replace_data["head_field"]["DATE"] == ""){ //チェックボックスにチェックがないまたは日付が空
            $flg = "NOINPUT";
            $this->replace_data["head_field"]["DATE"] = CTRL_DATE;
            if(VARS::post("REPLACEHIDDENDATE")==""){
                $flg2 = "NULL";
                $this->replace_data["head_field"]["DATE"] = CTRL_DATE;
            }
        }
        for ($i = 0; $i < get_count($array); $i++)
        {
            //MEDEXAM_HDATが存在するかチェックする
            $query = knjf010Query::checkMedexam_hdat($this->year, $array[$i]);
            $data = $db->getRow($query, DB_FETCHMODE_ASSOC);
            if(!is_array($data)){
                $flag = "noRecord"; //レコードが存在しない
            } elseif (!$data["DATE"]) {
                $flag = "noDate"; //レコードが存在してもDATEフィールドが空
            }
            if($flg == "NOINPUT" && ($flag == "noRecord" || $flag == "noDate")){
                $query = knjf010Query::ReplaceUpdate_Head($this,$array[$i],$flag);
            }
            elseif($flg != "NOINPUT"){
                $query = knjf010Query::ReplaceUpdate_Head($this,$array[$i],$flag);
            }
            elseif($flg2 == "NULL"){
                $query = knjf010Query::ReplaceUpdate_Head($this,$array[$i],$flag);
            }
            else{
                continue;
            }
            $flag = "";     //フラグを初期化

            $db->query($query);
        }
        $db->commit();
        Query::dbCheckIn($db);
    }
    //MEDEXAM_DET_DATの更新
    function &updateDet()
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        $array = explode(",", $this->replace_data["selectdata"]);
        for ($i = 0; $i < get_count($array); $i++)
        {
            //MEDEXAM_DET_DATが存在するかチェックする
            $data = common::GetMasterData("SELECT SCHREGNO FROM MEDEXAM_DET_DAT WHERE YEAR = '".$this->year."' AND SCHREGNO = '".$array[$i]."'");
            if($data == ""){
                $flag = "insert";
            }
            $query = knjf010Query::ReplaceUpdate_Det($this,$array[$i],$flag);
            $flag = "";     //フラグを初期化
            $db->query($query);
        }
        $db->commit();
        Query::dbCheckIn($db);
    }
}
?>