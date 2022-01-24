<?php

require_once('for_php7.php');
class knjf140jModel extends Model
{
    public $field    = array();
    public $data     = array();
    public $cmd;
    public $control;   //コントロールマスタ
    public $staffcd;   //登録者コード
    public $schoolCd;
    public $getPrgId;  //コールされたか
    public $sendAuth;  //親画面の権限
    public $auth;      //権限
    public $z010name1;

    public function knjf140jModel()
    {
    }

    public function init()
    {
        if (VARS::get("PROGRAMID")) {
            unset($this->getPrgId);
            unset($this->sendAuth);
            unset($this->auth);
        }
        if (VARS::get("SEND_PRGID")) {
            $this->getPrgId = VARS::get("SEND_PRGID");
            $this->sendAuth = VARS::get("SEND_AUTH");
        }
        $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;

        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);

        $this->field = array("OUTPUT"         => VARS::post("OUTPUT"),         //出力取込種別
                             "SCHOOL_KIND"    => VARS::post("SCHOOL_KIND"),    //校種
                             "YEAR"           => VARS::post("YEAR"),           //年度一覧
                             "GRADE_HR_CLASS" => VARS::post("GRADE_HR_CLASS"), //年組一覧
                             "HEADER"         => VARS::post("HEADER"),         //ヘッダ有
                             "CSVCD"          => VARS::post("CSVCD"),          //対象データ
                             "SHORI_MEI"      => VARS::post("SHORI_MEI")       //処理名
                             );

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll(); //プロパティファイル
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
        if (VARS::file("FILE")) {
            $this->field["FILE"] = VARS::file("FILE");        //ファイルのアップロード
            $this->field["FILE"]["name"] = i18n_convert($this->field["FILE"]["name"], "UTF-8", "SJIS-win");
        }
        //学校を判断
        $db = Query::dbCheckOut();
        $this->z010name1 = $db->getOne(knjf140jQuery::getSchoolName());
        Query::dbCheckIn($db);
    }

    //ファイル取り込み
    public function &getExecModel()
    {
        $this->dataArr = array();
        $this->dataArr = $this->getFileString();
        $disableCodes[] = array();
        if ($this->isDataMatched($this->dataArr)) {
            $errFlg = $this->checkCsvErrQuery(PROGRAMID);
            if ($errFlg) {
                $db = Query::dbCheckOut();
                $db->autoCommit(false);
                for ($i = 0; $i < get_count($this->dataArr); $i++) {
                    //更新
                    if ($this->field["SHORI_MEI"] == "1") {
                        if ($this->field["CSVCD"] == "1") { //一般
                            $disableCodes = array(  "F030"  //栄養状態
                                                  , "F040"  //脊柱・胸郭・四肢（全般）
                                                  , "F041"  //脊柱・胸郭・四肢（疾患1～3）
                                                  , "F050"  //目の疾病及び異常
                                                  , "F051"  //色覚異常
                                                  , "F060"  //耳鼻咽頭疾患（全般）
                                                  , "F061"  //耳鼻咽頭疾患（疾患1～3）
                                                  , "F070"  //皮膚疾患
                                                  , "F100"  //結核（所見）
                                                  , "F120"  //結核（その他の検査）疾病及び異常
                                                  , "F080"  //心臓　臨床医学的検査（心電図）
                                                  , "F091"  //心臓（精密検査）
                                                  , "F090"  //心臓（疾病及び異常）
                                                  , "F146"  //尿（精密検査）
                                                  , "F150"  //事後措置1
                                                  , "F151"  //事後措置2
                                                  );
                        } else { //歯・口腔
                            $disableCodes = array(  "F530"  //その他の疾病及び異常
                                                  , "F531"  //口腔の疾病及び異常
                                                  , "F540"  //所見
                                                  , "F541"  //事後措置
                                                  );
                        }
                        foreach ($disableCodes as $code) {
                            $disableCodes[$code] = $this->getDisableCodes($db, $code, $data["YEAR"]);
                        }
                        //delete and insert
                        knjf140jQuery::deleteinsertQueryCsv($db, $this->dataArr[$i], $this->field["CSVCD"], $this, $disableCodes);
                    //削除
                    } else {
                        //delete
                        knjf140jQuery::deleteQueryCsv($db, $this->dataArr[$i], $this->field["CSVCD"]);
                    }
                }
                $db->commit();
                Query::dbCheckIn($db);
                $this->setMessage("MSG202", "更新件数".get_count($this->dataArr)."件");
            }
        }
    }

    public function getFileString()
    {
        //ファイル名取得
        $target = $_FILES['FILE']['name'];
        $fileInfo = pathinfo($target);
        $kakutyousi = $fileInfo['extension'];
        $kakutyousi = strtoupper($kakutyousi);
        //拡張子チェック
        if ($kakutyousi != "CSV") {
            $this->warning = "CSVファイルを指定して下さい。\\n(".$kakutyousi."ファイルの取込は出来ません。)";
            return false;
        }

        $tempArr = array();
        if (is_uploaded_file($this->field["FILE"]["tmp_name"])) { //HTTP POSTによりアップロードされたファイルかどうかを調べる
            $checkdata = array();
            if ($this->field["CSVCD"] == "1") { //一般
                $checkdata = array();
                $checkdata["YEAR"]                      = array(4, "年度");
                $checkdata["DATE"]                      = array(10, "健診実施日");
                $checkdata["SCHREGNO"]                  = array(8, "学籍番号");
                $checkdata["HEIGHT"]                    = array(5, "身長");
                $checkdata["WEIGHT"]                    = array(5, "体重");
                $checkdata["R_VISION_CANTMEASURE"]      = array(1, "視力(右)測定困難");
                $checkdata["L_VISION_CANTMEASURE"]      = array(1, "視力(左)測定困難");
                $checkdata["R_BAREVISION_MARK"]         = array(2, "視力右裸眼(記号)", "F017");
                $checkdata["L_BAREVISION_MARK"]         = array(2, "視力左裸眼(記号)", "F017");
                $checkdata["R_VISION_MARK"]             = array(2, "視力右矯正(記号)", "F017");
                $checkdata["L_VISION_MARK"]             = array(2, "視力左矯正(記号)", "F017");
                $checkdata["R_BAREVISION"]              = array(5, "視力右裸眼(数字)");
                $checkdata["L_BAREVISION"]              = array(5, "視力左裸眼(数字)");
                $checkdata["R_VISION"]                  = array(5, "視力右矯正(数字)");
                $checkdata["L_VISION"]                  = array(5, "視力左矯正(数字)");
                $checkdata["R_EAR_CANTMEASURE"]         = array(1, "聴力(右)測定困難");
                $checkdata["R_EAR_DB"]                  = array(5, "聴力右平均dB");
                $checkdata["R_EAR"]                     = array(2, "聴力右状態コード", "F010");
                $checkdata["L_EAR_CANTMEASURE"]         = array(1, "聴力(左)測定困難");
                $checkdata["L_EAR_DB"]                  = array(5, "聴力左平均dB");
                $checkdata["L_EAR"]                     = array(2, "聴力左状態コード", "F010");
                $checkdata["NUTRITIONCD"]               = array(2, "栄養状態コード", "F030");
                $checkdata["NUTRITIONCD_REMARK"]        = array(60, "栄養状態所見");
                $checkdata["SPINERIBCD"]                = array(2, "脊柱胸郭四肢（全般）コード", "F040");
                $checkdata["SPINERIBCD_REMARK"]         = array(60, "脊柱胸郭四肢（全般）所見");
                $checkdata["SPINERIBCD1"]               = array(2, "脊柱胸郭四肢（疾患1）コード", "F041");
                $checkdata["SPINERIBCD_REMARK1"]        = array(60, "脊柱胸郭四肢（疾患1）所見");
                $checkdata["SPINERIBCD2"]               = array(2, "脊柱胸郭四肢（疾患2）コード", "F041");
                $checkdata["SPINERIBCD_REMARK2"]        = array(60, "脊柱胸郭四肢（疾患2）所見");
                $checkdata["SPINERIBCD3"]               = array(2, "脊柱胸郭四肢（疾患3）コード", "F041");
                $checkdata["SPINERIBCD_REMARK3"]        = array(60, "脊柱胸郭四肢（疾患3）所見");
                $checkdata["EYEDISEASECD"]              = array(2, "目疾病及異常コード", "F050");
                $checkdata["EYE_TEST_RESULT"]           = array(60, "目の疾病及び異常所見");
                $checkdata["EYEDISEASECD5"]             = array(2, "色覚異常コード", "F051");
                $checkdata["VISION_CANTMEASURE"]        = array(60, "色覚異常所見");
                $checkdata["NOSEDISEASECD"]             = array(2, "耳鼻咽頭疾患（全般）コード", "F060");
                $checkdata["NOSEDISEASECD_REMARK"]      = array(60, "耳鼻咽頭疾患（全般）所見");
                $checkdata["NOSEDISEASECD5"]            = array(2, "耳鼻咽頭疾患（疾患1）コード", "F061");
                $checkdata["NOSEDISEASECD_REMARK1"]     = array(60, "耳鼻咽頭疾患（疾患1）所見");
                $checkdata["NOSEDISEASECD6"]            = array(2, "耳鼻咽頭疾患（疾患2）コード", "F061");
                $checkdata["NOSEDISEASECD_REMARK2"]     = array(60, "耳鼻咽頭疾患（疾患2）所見");
                $checkdata["NOSEDISEASECD7"]            = array(2, "耳鼻咽頭疾患（疾患3）コード", "F061");
                $checkdata["NOSEDISEASECD_REMARK3"]     = array(60, "耳鼻咽頭疾患（疾患3）所見");
                $checkdata["SKINDISEASECD"]             = array(2, "皮膚疾患コード", "F070");
                $checkdata["SKINDISEASECD_REMARK"]      = array(60, "皮膚疾患所見");
                $checkdata["TB_FILMDATE"]               = array(10, "結核撮影日付");
                $checkdata["TB_FILMNO"]                 = array(6, "結核フィルム番号");
                $checkdata["TB_REMARKCD"]               = array(2, "結核所見コード", "F100");
                $checkdata["TB_X_RAY"]                  = array(60, "結核所見");
                $checkdata["TB_RE_EXAMINATION_DATE"]    = array(10, "結核再検査日付");
                $checkdata["TB_RE_EXAMINATION_RESULT"]  = array(2, "結核再検査結果", "F101");
                $checkdata["TB_RE_EXAMINATION_FILMNO"]  = array(6, "結核再検査フィルム番号");
                $checkdata["TB_OTHERTESTCD"]            = array(2, "結核その他検査コード", "F110");
                $checkdata["TB_NAMECD"]                 = array(2, "結核疾病及び異常コード", "F120");
                $checkdata["TB_NAME_REMARK1"]           = array(60, "結核疾病及び異常所見");
                $checkdata["TB_ADVISECD"]               = array(2, "結核指導区分コード", "F130");
                $checkdata["HEART_MEDEXAM"]             = array(2, "心臓臨床医学的検査コード", "F080");
                $checkdata["HEART_MEDEXAM_REMARK"]      = array(120, "心臓臨床医学的検査");
                $checkdata["MANAGEMENT_DIV"]            = array(2, "心臓精密検査コード", "F091");
                $checkdata["MANAGEMENT_REMARK"]         = array(120, "心臓精密検査所見");
                $checkdata["HEARTDISEASECD"]            = array(2, "心臓疾病及異常コード", "F090");
                $checkdata["HEARTDISEASECD_REMARK"]     = array(60, "心臓疾病及異常所見");
                $checkdata["ALBUMINURIA1CD"]            = array(2, "一次尿蛋白コード", "F020");
                $checkdata["URICSUGAR1CD"]              = array(2, "一次尿糖コード", "F019");
                $checkdata["URICBLEED1CD"]              = array(2, "一次尿潜血コード", "F018");
                $checkdata["ALBUMINURIA2CD"]            = array(2, "再検査尿蛋白コード", "F020");
                $checkdata["URICSUGAR2CD"]              = array(2, "再検査尿糖コード", "F019");
                $checkdata["URICBLEED2CD"]              = array(2, "再検査尿潜血コード", "F018");
                $checkdata["DETAILED_EXAMINATION"]      = array(2, "尿精密検査コード", "F146");
                $checkdata["URI_ADVISECD"]              = array(2, "尿精密検査指導区分コード", "F021");
                $checkdata["URICOTHERTEST"]             = array(60, "尿精密検査所見");
                $checkdata["OTHER_REMARK"]              = array(60, "その他疾病及異常所見");
                $checkdata["DOC_DATE"]                  = array(10, "学校医日付");
                $checkdata["DOC_REMARK"]                = array(60, "学校医所見");
                $checkdata["TREATCD"]                   = array(2, "事後処置1コード", "F150");
                $checkdata["TREAT_REMARK1"]             = array(60, "事後処置1所見");
                $checkdata["TREATCD2"]                  = array(2, "事後処置2コード", "F151");
                $checkdata["TREATCD2_REMARK1"]          = array(60, "事後処置2所見");
                $checkdata["REMARK"]                    = array(180, "備考");
                $checkdata["MEDICAL_HISTORY1"]          = array(2, "既往症2", "F143");
                $checkdata["MEDICAL_HISTORY2"]          = array(2, "既往症2", "F143");
                $checkdata["MEDICAL_HISTORY3"]          = array(2, "既往症3", "F143");
                $checkdata["DIAGNOSIS_NAME"]            = array(150, "診断名");
            } else { //歯・口腔
                $checkdata = array();
                $checkdata["YEAR"]              = array(4, "年度");
                $checkdata["TOOTH_DATE"]        = array(10, "健診実施日");
                $checkdata["SCHREGNO"]          = array(8, "学籍番号");
                $checkdata["JAWS_JOINTCD"]      = array(2, "歯列・咬合コード", "F510");
                $checkdata["JAWS_JOINTCD2"]     = array(2, "顎関節コード", "F511");
                $checkdata["PLAQUECD"]          = array(2, "歯垢状態コード", "F520");
                $checkdata["GUMCD"]             = array(2, "歯肉状態コード", "F513");
                $checkdata["CALCULUS"]          = array(2, "歯石沈着", "F521");
                $checkdata["ORTHODONTICS"]      = array(2, "矯正の有無");
                $checkdata["UP_R_BABY5"]        = array(2, "乳歯 上 右E", "F550");
                $checkdata["UP_R_BABY4"]        = array(2, "乳歯 上 右D", "F550");
                $checkdata["UP_R_BABY3"]        = array(2, "乳歯 上 右C", "F550");
                $checkdata["UP_R_BABY2"]        = array(2, "乳歯 上 右B", "F550");
                $checkdata["UP_R_BABY1"]        = array(2, "乳歯 上 右A", "F550");
                $checkdata["UP_L_BABY1"]        = array(2, "乳歯 上 左A", "F550");
                $checkdata["UP_L_BABY2"]        = array(2, "乳歯 上 左B", "F550");
                $checkdata["UP_L_BABY3"]        = array(2, "乳歯 上 左C", "F550");
                $checkdata["UP_L_BABY4"]        = array(2, "乳歯 上 左D", "F550");
                $checkdata["UP_L_BABY5"]        = array(2, "乳歯 上 左E", "F550");
                $checkdata["LW_R_BABY5"]        = array(2, "乳歯 下 右E", "F550");
                $checkdata["LW_R_BABY4"]        = array(2, "乳歯 下 右D", "F550");
                $checkdata["LW_R_BABY3"]        = array(2, "乳歯 下 右C", "F550");
                $checkdata["LW_R_BABY2"]        = array(2, "乳歯 下 右B", "F550");
                $checkdata["LW_R_BABY1"]        = array(2, "乳歯 下 右A", "F550");
                $checkdata["LW_L_BABY1"]        = array(2, "乳歯 下 左A", "F550");
                $checkdata["LW_L_BABY2"]        = array(2, "乳歯 下 左B", "F550");
                $checkdata["LW_L_BABY3"]        = array(2, "乳歯 下 左C", "F550");
                $checkdata["LW_L_BABY4"]        = array(2, "乳歯 下 左D", "F550");
                $checkdata["LW_L_BABY5"]        = array(2, "乳歯 下 左E", "F550");
                $checkdata["BABYTOOTH"]         = array(2, "乳歯現在数");
                $checkdata["REMAINBABYTOOTH"]   = array(2, "乳歯未処置数");
                $checkdata["TREATEDBABYTOOTH"]  = array(2, "乳歯処置数");
                $checkdata["BRACK_BABYTOOTH"]   = array(2, "要注意乳歯数");
                $checkdata["UP_R_ADULT8"]       = array(2, "永久歯 上 右8", "F550");
                $checkdata["UP_R_ADULT7"]       = array(2, "永久歯 上 右7", "F550");
                $checkdata["UP_R_ADULT6"]       = array(2, "永久歯 上 右6", "F550");
                $checkdata["UP_R_ADULT5"]       = array(2, "永久歯 上 右5", "F550");
                $checkdata["UP_R_ADULT4"]       = array(2, "永久歯 上 右4", "F550");
                $checkdata["UP_R_ADULT3"]       = array(2, "永久歯 上 右3", "F550");
                $checkdata["UP_R_ADULT2"]       = array(2, "永久歯 上 右2", "F550");
                $checkdata["UP_R_ADULT1"]       = array(2, "永久歯 上 右1", "F550");
                $checkdata["UP_L_ADULT1"]       = array(2, "永久歯 上 左1", "F550");
                $checkdata["UP_L_ADULT2"]       = array(2, "永久歯 上 左2", "F550");
                $checkdata["UP_L_ADULT3"]       = array(2, "永久歯 上 左3", "F550");
                $checkdata["UP_L_ADULT4"]       = array(2, "永久歯 上 左4", "F550");
                $checkdata["UP_L_ADULT5"]       = array(2, "永久歯 上 左5", "F550");
                $checkdata["UP_L_ADULT6"]       = array(2, "永久歯 上 左6", "F550");
                $checkdata["UP_L_ADULT7"]       = array(2, "永久歯 上 左7", "F550");
                $checkdata["UP_L_ADULT8"]       = array(2, "永久歯 上 左8", "F550");
                $checkdata["LW_R_ADULT8"]       = array(2, "永久歯 下 右8", "F550");
                $checkdata["LW_R_ADULT7"]       = array(2, "永久歯 下 右7", "F550");
                $checkdata["LW_R_ADULT6"]       = array(2, "永久歯 下 右6", "F550");
                $checkdata["LW_R_ADULT5"]       = array(2, "永久歯 下 右5", "F550");
                $checkdata["LW_R_ADULT4"]       = array(2, "永久歯 下 右4", "F550");
                $checkdata["LW_R_ADULT3"]       = array(2, "永久歯 下 右3", "F550");
                $checkdata["LW_R_ADULT2"]       = array(2, "永久歯 下 右2", "F550");
                $checkdata["LW_R_ADULT1"]       = array(2, "永久歯 下 右1", "F550");
                $checkdata["LW_L_ADULT1"]       = array(2, "永久歯 下 左1", "F550");
                $checkdata["LW_L_ADULT2"]       = array(2, "永久歯 下 左2", "F550");
                $checkdata["LW_L_ADULT3"]       = array(2, "永久歯 下 左3", "F550");
                $checkdata["LW_L_ADULT4"]       = array(2, "永久歯 下 左4", "F550");
                $checkdata["LW_L_ADULT5"]       = array(2, "永久歯 下 左5", "F550");
                $checkdata["LW_L_ADULT6"]       = array(2, "永久歯 下 左6", "F550");
                $checkdata["LW_L_ADULT7"]       = array(2, "永久歯 下 左7", "F550");
                $checkdata["LW_L_ADULT8"]       = array(2, "永久歯 下 左8", "F550");
                $checkdata["ADULTTOOTH"]        = array(2, "永久歯数");
                $checkdata["REMAINADULTTOOTH"]  = array(2, "永久歯未処置数");
                $checkdata["TREATEDADULTTOOTH"] = array(2, "永久歯処置数");
                $checkdata["LOSTADULTTOOTH"]    = array(2, "永久歯喪失数");
                $checkdata["BRACK_ADULTTOOTH"]  = array(2, "要観察歯数");
                $checkdata["OTHERDISEASECD"]    = array(2, "その他疾病及び異常コード", "F530");
                $checkdata["OTHERDISEASE"]      = array(60, "その他疾病及び異常");
                $checkdata["OTHERDISEASECD2"]   = array(2, "口腔の疾病及び異常コード", "F531");
                $checkdata["OTHERDISEASE2"]     = array(60, "口腔の疾病及び異常");
                $checkdata["DENTISTREMARKCD"]   = array(2, "学校歯科医所見コード1", "F540");
                $checkdata["DENTISTREMARK"]     = array(60, "学校歯科医所見1");
                $checkdata["DENTISTREMARKCD2"]  = array(2, "学校歯科医所見コード2", "F540");
                $checkdata["DENTISTREMARK2"]    = array(60, "学校歯科医所見2");
                $checkdata["DENTISTREMARKCD3"]  = array(2, "学校歯科医所見コード3", "F540");
                $checkdata["DENTISTREMARK3"]    = array(60, "学校歯科医所見3");
                $checkdata["DENTISTREMARKDATE"] = array(10, "学校歯科医所見日付");
                $checkdata["DENTISTTREATCD"]    = array(2, "学校歯科医事後処置コード1", "F541");
                $checkdata["DENTISTTREAT"]      = array(60, "学校歯科医事後処置1");
                $checkdata["DENTISTTREATCD2"]   = array(2, "学校歯科医事後処置コード2", "F541");
                $checkdata["DENTISTTREAT2"]     = array(60, "学校歯科医事後処置2");
            }

            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            //エラーデータの削除
            $query = $this->deleteQueryErr(PROGRAMID);
            $result = $db->query($query);

            $i = 0;
            $record_no = 1;            //行番号(初期値)
            $primaryKey1    = array();
            $primaryKey2    = array();
            $primaryKey3    = array();
            $duplicateCsv1  = array();
            $duplicateCsv2  = array();
            $duplicateCsv3  = array();
            if ($this->field["CSVCD"] == "1") {
                $primaryKey1 = $this->getPrimaryKey('MEDEXAM_DET_DAT');
                $primaryKey2 = $this->getPrimaryKey('MEDEXAM_DET_DETAIL_DAT');
            } else {
                $primaryKey1 = $this->getPrimaryKey('MEDEXAM_TOOTH_DAT');
                $primaryKey2 = $this->getPrimaryKey('MEDEXAM_TOOTH_DETAIL_DAT');
            }
            $primaryKey3 = $this->getPrimaryKey('MEDEXAM_HDAT');

            $fp = fopen($this->field["FILE"]["tmp_name"], "r"); //ファイルまたはURLをオープンする
            while (!feof($fp)) {                                //ファイルポインタがファイル終端に達しているかどうか調べる
                $buffer = fgets($fp, 4096);                     //ファイルポインタから1行取得する
                if (trim($buffer) != '') {
                    //ヘッダ有なら１行目は無視
                    if ($this->field["HEADER"] == "on" && $record_no<2) {
                        $record_no++;
                        continue;
                    }
                    $buffer = i18n_convert($buffer, "UTF-8", "SJIS-win");        //文字列を指定コードに変換する

                    $tmp = explode(",", $buffer);

                    if (in_array($this->Properties["printKenkouSindanIppan"], array("1", "2", "3"))) {
                        $Ippan = $this->Properties["printKenkouSindanIppan"];
                    } else {
                        $Ippan = "0";
                    }

                    if ($this->field["CSVCD"] == "1") { //一般
                        $tmpCnt = 0;
                        $data["CSVCD"]                  = trim($tmp[$tmpCnt++]);
                        $data["YEAR"]                   = trim($tmp[$tmpCnt++]);
                        $data["DATE"]                   = str_replace("/", "-", trim($tmp[$tmpCnt++]));
                        $data["SCHREGNO"]               = trim($tmp[$tmpCnt++]);
                        $tmpCnt++;
                        $tmpCnt++;
                        $tmpCnt++;
                        $tmpCnt++;
                        $data["HEIGHT"]                   = trim($tmp[$tmpCnt++]);
                        $data["WEIGHT"]                   = trim($tmp[$tmpCnt++]);
                        $data["R_VISION_CANTMEASURE"]     = trim($tmp[$tmpCnt++]);
                        $data["L_VISION_CANTMEASURE"]     = trim($tmp[$tmpCnt++]);
                        $data["R_BAREVISION_MARK"]        = trim($tmp[$tmpCnt++]);
                        $data["L_BAREVISION_MARK"]        = trim($tmp[$tmpCnt++]);
                        $data["R_VISION_MARK"]            = trim($tmp[$tmpCnt++]);
                        $data["L_VISION_MARK"]            = trim($tmp[$tmpCnt++]);
                        $data["R_BAREVISION"]             = trim($tmp[$tmpCnt++]);
                        $data["L_BAREVISION"]             = trim($tmp[$tmpCnt++]);
                        $data["R_VISION"]                 = trim($tmp[$tmpCnt++]);
                        $data["L_VISION"]                 = trim($tmp[$tmpCnt++]);
                        $data["R_EAR_CANTMEASURE"]        = trim($tmp[$tmpCnt++]);
                        $data["R_EAR_DB"]                 = trim($tmp[$tmpCnt++]);
                        $data["R_EAR"]                    = trim($tmp[$tmpCnt++]);
                        $data["L_EAR_CANTMEASURE"]        = trim($tmp[$tmpCnt++]);
                        $data["L_EAR_DB"]                 = trim($tmp[$tmpCnt++]);
                        $data["L_EAR"]                    = trim($tmp[$tmpCnt++]);
                        $data["NUTRITIONCD"]              = trim($tmp[$tmpCnt++]);
                        $data["NUTRITIONCD_REMARK"]       = trim($tmp[$tmpCnt++]);
                        $data["SPINERIBCD"]               = trim($tmp[$tmpCnt++]);
                        $data["SPINERIBCD_REMARK"]        = trim($tmp[$tmpCnt++]);
                        $data["SPINERIBCD1"]              = trim($tmp[$tmpCnt++]);
                        $data["SPINERIBCD_REMARK1"]       = trim($tmp[$tmpCnt++]);
                        $data["SPINERIBCD2"]              = trim($tmp[$tmpCnt++]);
                        $data["SPINERIBCD_REMARK2"]       = trim($tmp[$tmpCnt++]);
                        $data["SPINERIBCD3"]              = trim($tmp[$tmpCnt++]);
                        $data["SPINERIBCD_REMARK3"]       = trim($tmp[$tmpCnt++]);
                        $data["EYEDISEASECD"]             = trim($tmp[$tmpCnt++]);
                        $data["EYE_TEST_RESULT"]          = trim($tmp[$tmpCnt++]);
                        $data["EYEDISEASECD5"]            = trim($tmp[$tmpCnt++]);
                        $data["VISION_CANTMEASURE"]       = trim($tmp[$tmpCnt++]);
                        $data["NOSEDISEASECD"]            = trim($tmp[$tmpCnt++]);
                        $data["NOSEDISEASECD_REMARK"]     = trim($tmp[$tmpCnt++]);
                        $data["NOSEDISEASECD5"]           = trim($tmp[$tmpCnt++]);
                        $data["NOSEDISEASECD_REMARK1"]    = trim($tmp[$tmpCnt++]);
                        $data["NOSEDISEASECD6"]           = trim($tmp[$tmpCnt++]);
                        $data["NOSEDISEASECD_REMARK2"]    = trim($tmp[$tmpCnt++]);
                        $data["NOSEDISEASECD7"]           = trim($tmp[$tmpCnt++]);
                        $data["NOSEDISEASECD_REMARK3"]    = trim($tmp[$tmpCnt++]);
                        $data["SKINDISEASECD"]            = trim($tmp[$tmpCnt++]);
                        $data["SKINDISEASECD_REMARK"]     = trim($tmp[$tmpCnt++]);
                        $data["TB_FILMDATE"]              = str_replace("/", "-", trim($tmp[$tmpCnt++]));
                        $data["TB_FILMNO"]                = trim($tmp[$tmpCnt++]);
                        $data["TB_REMARKCD"]              = trim($tmp[$tmpCnt++]);
                        $data["TB_X_RAY"]                 = trim($tmp[$tmpCnt++]);
                        $data["TB_RE_EXAMINATION_DATE"]   = str_replace("/", "-", trim($tmp[$tmpCnt++]));
                        $data["TB_RE_EXAMINATION_RESULT"] = trim($tmp[$tmpCnt++]);
                        $data["TB_RE_EXAMINATION_FILMNO"] = trim($tmp[$tmpCnt++]);
                        $data["TB_OTHERTESTCD"]           = trim($tmp[$tmpCnt++]);
                        $data["TB_NAMECD"]                = trim($tmp[$tmpCnt++]);
                        $data["TB_NAME_REMARK1"]          = trim($tmp[$tmpCnt++]);
                        $data["TB_ADVISECD"]              = trim($tmp[$tmpCnt++]);
                        $data["HEART_MEDEXAM"]            = trim($tmp[$tmpCnt++]);
                        $data["HEART_MEDEXAM_REMARK"]     = trim($tmp[$tmpCnt++]);
                        $data["MANAGEMENT_DIV"]           = trim($tmp[$tmpCnt++]);
                        $data["MANAGEMENT_REMARK"]        = trim($tmp[$tmpCnt++]);
                        $data["HEARTDISEASECD"]           = trim($tmp[$tmpCnt++]);
                        $data["HEARTDISEASECD_REMARK"]    = trim($tmp[$tmpCnt++]);
                        $data["ALBUMINURIA1CD"]           = trim($tmp[$tmpCnt++]);
                        $data["URICSUGAR1CD"]             = trim($tmp[$tmpCnt++]);
                        $data["URICBLEED1CD"]             = trim($tmp[$tmpCnt++]);
                        $data["ALBUMINURIA2CD"]           = trim($tmp[$tmpCnt++]);
                        $data["URICSUGAR2CD"]             = trim($tmp[$tmpCnt++]);
                        $data["URICBLEED2CD"]             = trim($tmp[$tmpCnt++]);
                        $data["DETAILED_EXAMINATION"]     = trim($tmp[$tmpCnt++]);
                        $data["URI_ADVISECD"]             = trim($tmp[$tmpCnt++]);
                        $data["URICOTHERTEST"]            = trim($tmp[$tmpCnt++]);
                        $data["OTHER_REMARK"]             = trim($tmp[$tmpCnt++]);
                        $data["DOC_DATE"]                 = str_replace("/", "-", trim($tmp[$tmpCnt++]));
                        $data["DOC_REMARK"]               = trim($tmp[$tmpCnt++]);
                        $data["TREATCD"]                  = trim($tmp[$tmpCnt++]);
                        $data["TREAT_REMARK1"]            = trim($tmp[$tmpCnt++]);
                        $data["TREATCD2"]                 = trim($tmp[$tmpCnt++]);
                        $data["TREATCD2_REMARK1"]         = trim($tmp[$tmpCnt++]);
                        $data["REMARK"]                   = trim($tmp[$tmpCnt++]);
                        $data["MEDICAL_HISTORY1"]         = trim($tmp[$tmpCnt++]);
                        $data["MEDICAL_HISTORY2"]         = trim($tmp[$tmpCnt++]);
                        $data["MEDICAL_HISTORY3"]         = trim($tmp[$tmpCnt++]);
                        $data["DIAGNOSIS_NAME"]           = trim($tmp[$tmpCnt++]);
                        $data["DIAGNOSIS_NAME"]           = "";
                    } else { //歯・口腔
                        $tmpCnt = 0;
                        $data["CSVCD"]                  = trim($tmp[$tmpCnt++]);
                        $data["YEAR"]                   = trim($tmp[$tmpCnt++]);
                        $data["TOOTH_DATE"]             = str_replace("/", "-", trim($tmp[$tmpCnt++]));
                        $data["SCHREGNO"]               = trim($tmp[$tmpCnt++]);
                        $tmpCnt++;
                        $tmpCnt++;
                        $tmpCnt++;
                        $tmpCnt++;
                        $data["JAWS_JOINTCD2"]          = trim($tmp[$tmpCnt++]);
                        $data["JAWS_JOINTCD"]           = trim($tmp[$tmpCnt++]);
                        $data["PLAQUECD"]               = trim($tmp[$tmpCnt++]);
                        $data["GUMCD"]                  = trim($tmp[$tmpCnt++]);
                        $data["CALCULUS"]               = trim($tmp[$tmpCnt++]);
                        $data["ORTHODONTICS"]           = trim($tmp[$tmpCnt++]);
                        $data["UP_R_BABY5"]             = trim($tmp[$tmpCnt++]);
                        $data["UP_R_BABY4"]             = trim($tmp[$tmpCnt++]);
                        $data["UP_R_BABY3"]             = trim($tmp[$tmpCnt++]);
                        $data["UP_R_BABY2"]             = trim($tmp[$tmpCnt++]);
                        $data["UP_R_BABY1"]             = trim($tmp[$tmpCnt++]);
                        $data["UP_L_BABY1"]             = trim($tmp[$tmpCnt++]);
                        $data["UP_L_BABY2"]             = trim($tmp[$tmpCnt++]);
                        $data["UP_L_BABY3"]             = trim($tmp[$tmpCnt++]);
                        $data["UP_L_BABY4"]             = trim($tmp[$tmpCnt++]);
                        $data["UP_L_BABY5"]             = trim($tmp[$tmpCnt++]);
                        $data["LW_R_BABY5"]             = trim($tmp[$tmpCnt++]);
                        $data["LW_R_BABY4"]             = trim($tmp[$tmpCnt++]);
                        $data["LW_R_BABY3"]             = trim($tmp[$tmpCnt++]);
                        $data["LW_R_BABY2"]             = trim($tmp[$tmpCnt++]);
                        $data["LW_R_BABY1"]             = trim($tmp[$tmpCnt++]);
                        $data["LW_L_BABY1"]             = trim($tmp[$tmpCnt++]);
                        $data["LW_L_BABY2"]             = trim($tmp[$tmpCnt++]);
                        $data["LW_L_BABY3"]             = trim($tmp[$tmpCnt++]);
                        $data["LW_L_BABY4"]             = trim($tmp[$tmpCnt++]);
                        $data["LW_L_BABY5"]             = trim($tmp[$tmpCnt++]);
                        $data["BABYTOOTH"]              = trim($tmp[$tmpCnt++]);
                        $data["REMAINBABYTOOTH"]        = trim($tmp[$tmpCnt++]);
                        $data["TREATEDBABYTOOTH"]       = trim($tmp[$tmpCnt++]);
                        $data["BRACK_BABYTOOTH"]        = trim($tmp[$tmpCnt++]);
                        $data["UP_R_ADULT8"]            = trim($tmp[$tmpCnt++]);
                        $data["UP_R_ADULT7"]            = trim($tmp[$tmpCnt++]);
                        $data["UP_R_ADULT6"]            = trim($tmp[$tmpCnt++]);
                        $data["UP_R_ADULT5"]            = trim($tmp[$tmpCnt++]);
                        $data["UP_R_ADULT4"]            = trim($tmp[$tmpCnt++]);
                        $data["UP_R_ADULT3"]            = trim($tmp[$tmpCnt++]);
                        $data["UP_R_ADULT2"]            = trim($tmp[$tmpCnt++]);
                        $data["UP_R_ADULT1"]            = trim($tmp[$tmpCnt++]);
                        $data["UP_L_ADULT1"]            = trim($tmp[$tmpCnt++]);
                        $data["UP_L_ADULT2"]            = trim($tmp[$tmpCnt++]);
                        $data["UP_L_ADULT3"]            = trim($tmp[$tmpCnt++]);
                        $data["UP_L_ADULT4"]            = trim($tmp[$tmpCnt++]);
                        $data["UP_L_ADULT5"]            = trim($tmp[$tmpCnt++]);
                        $data["UP_L_ADULT6"]            = trim($tmp[$tmpCnt++]);
                        $data["UP_L_ADULT7"]            = trim($tmp[$tmpCnt++]);
                        $data["UP_L_ADULT8"]            = trim($tmp[$tmpCnt++]);
                        $data["LW_R_ADULT8"]            = trim($tmp[$tmpCnt++]);
                        $data["LW_R_ADULT7"]            = trim($tmp[$tmpCnt++]);
                        $data["LW_R_ADULT6"]            = trim($tmp[$tmpCnt++]);
                        $data["LW_R_ADULT5"]            = trim($tmp[$tmpCnt++]);
                        $data["LW_R_ADULT4"]            = trim($tmp[$tmpCnt++]);
                        $data["LW_R_ADULT3"]            = trim($tmp[$tmpCnt++]);
                        $data["LW_R_ADULT2"]            = trim($tmp[$tmpCnt++]);
                        $data["LW_R_ADULT1"]            = trim($tmp[$tmpCnt++]);
                        $data["LW_L_ADULT1"]            = trim($tmp[$tmpCnt++]);
                        $data["LW_L_ADULT2"]            = trim($tmp[$tmpCnt++]);
                        $data["LW_L_ADULT3"]            = trim($tmp[$tmpCnt++]);
                        $data["LW_L_ADULT4"]            = trim($tmp[$tmpCnt++]);
                        $data["LW_L_ADULT5"]            = trim($tmp[$tmpCnt++]);
                        $data["LW_L_ADULT6"]            = trim($tmp[$tmpCnt++]);
                        $data["LW_L_ADULT7"]            = trim($tmp[$tmpCnt++]);
                        $data["LW_L_ADULT8"]            = trim($tmp[$tmpCnt++]);
                        $data["ADULTTOOTH"]             = trim($tmp[$tmpCnt++]);
                        $data["REMAINADULTTOOTH"]       = trim($tmp[$tmpCnt++]);
                        $data["TREATEDADULTTOOTH"]      = trim($tmp[$tmpCnt++]);
                        $data["LOSTADULTTOOTH"]         = trim($tmp[$tmpCnt++]);
                        $data["BRACK_ADULTTOOTH"]       = trim($tmp[$tmpCnt++]);
                        $data["OTHERDISEASECD"]         = trim($tmp[$tmpCnt++]);
                        $data["OTHERDISEASE"]           = trim($tmp[$tmpCnt++]);
                        $data["OTHERDISEASECD2"]        = trim($tmp[$tmpCnt++]);
                        $data["OTHERDISEASE2"]          = trim($tmp[$tmpCnt++]);
                        $data["DENTISTREMARKCD"]        = trim($tmp[$tmpCnt++]);
                        $data["DENTISTREMARK"]          = trim($tmp[$tmpCnt++]);
                        $data["DENTISTREMARKCD2"]       = trim($tmp[$tmpCnt++]);
                        $data["DENTISTREMARK2"]         = trim($tmp[$tmpCnt++]);
                        $data["DENTISTREMARKCD3"]       = trim($tmp[$tmpCnt++]);
                        $data["DENTISTREMARK3"]         = trim($tmp[$tmpCnt++]);
                        $data["DENTISTREMARKDATE"]      = str_replace("/", "-", trim($tmp[$tmpCnt++]));
                        $data["DENTISTTREATCD"]         = trim($tmp[$tmpCnt++]);
                        $data["DENTISTTREAT"]           = trim($tmp[$tmpCnt++]);
                        $data["DENTISTTREATCD2"]        = trim($tmp[$tmpCnt++]);
                        $data["DENTISTTREAT2"]          = trim($tmp[$tmpCnt++]);

                        $data["ORTHODONTICS"] = $data["ORTHODONTICS"] ? $data["ORTHODONTICS"] : 0; //矯正の有無(初期値を0とする)
                        $data["DET_SEQ"]                = "";
                    }

                    if ($this->field["CSVCD"] == "1") {
                        if ($data["DATE"] != "" && $this->dateRep($data["DATE"])) {
                            $data["DATE"] = $this->dateRep($data["DATE"]);
                        }
                        if ($data["TB_FILMDATE"] != "" && $this->dateRep($data["TB_FILMDATE"])) {
                            $data["TB_FILMDATE"] = $this->dateRep($data["TB_FILMDATE"]);
                        }
                        if ($data["TB_RE_EXAMINATION_DATE"] != "" && $this->dateRep($data["TB_RE_EXAMINATION_DATE"])) {
                            $data["TB_RE_EXAMINATION_DATE"] = $this->dateRep($data["TB_RE_EXAMINATION_DATE"]);
                        }
                        if ($data["DOC_DATE"] != "" && $this->dateRep($data["DOC_DATE"])) {
                            $data["DOC_DATE"] = $this->dateRep($data["DOC_DATE"]);
                        }
                    } else {
                        if ($data["TOOTH_DATE"] != "" && $this->dateRep($data["TOOTH_DATE"])) {
                            $data["TOOTH_DATE"] = $this->dateRep($data["TOOTH_DATE"]);
                        }
                        if ($data["DENTISTREMARKDATE"] != "" && $this->dateRep($data["DENTISTREMARKDATE"])) {
                            $data["DENTISTREMARKDATE"] = $this->dateRep($data["DENTISTREMARKDATE"]);
                        }
                    }

                    //０埋め
                    $data = $this->zeroUme($data);
                    //エラーチェック
                    if ($this->errCheck($db, $data, $checkdata, $record_no, $duplicateCsv1, $duplicateCsv2, $duplicateCsv3, $primaryKey1, $primaryKey2, $primaryKey3)) {
                        $tempArr[$i] = $data;
                        $i++;
                    }
                    foreach ($primaryKey1 as $key => $val) {
                        $duplicateCsv1[$record_no][$val] = $data[$val];
                    }
                    foreach ($primaryKey2 as $key => $val) {
                        $duplicateCsv2[$record_no][$val] = $data[$val];
                    }
                    foreach ($primaryKey3 as $key => $val) {
                        $duplicateCsv3[$record_no][$val] = $data[$val];
                    }
                }//ifの綴り
                $record_no++; //行番号(カウント)
            }//whileの綴り
            $db->commit(); // トランザクションをコミットする。
            Query::dbCheckIn($db);
            fclose($fp);
        } else {
            $this->warning = "ファイル名が不正です。";
        }
        return $tempArr;
    }

    //エラーチェック
    public function errCheck($db, $data, $checkdata, $line, $duplicateCsv1, $duplicateCsv2, $duplicateCsv3, $primaryKey1, $primaryKey2, $primaryKey3)
    {
        $returnFlg          = true;
        $hissuErrFlg        = false;    //必須エラーフラグ
        $numericErrFlg      = false;    //数値エラーフラグ
        $lengthErrFlg       = false;    //レングスエラーフラグ
        $masterErrFlg       = false;    //存在エラーフラグ
        $valuesErrFlg       = false;    //値エラーフラグ
        $duplicateCsvErrFlg = false;    //重複(CSV)エラーフラグ
        $otherErrFlg        = false;    //その他エラーフラグ

        $hissuErrMsg        = "";       //必須エラーメッセージ
        $numericErrMsg      = "";       //数値エラーメッセージ
        $lengthErrMsg       = "";       //レングスエラーメッセージ
        $masterErrMsg       = "";       //存在エラーメッセージ
        $valuesErrMsg       = "";       //値エラーメッセージ
        $duplicateCsvErrMsg = "";       //重複(CSV)メッセージ
        $otherErrMsg        = "";       //その他エラーメッセージ

        $hissuSep        = "";
        $numericSep      = "";
        $lengthSep       = "";
        $masterSep       = "";
        $valuesSep       = "";
        $duplicateCsvSep = "";
        $otherSep        = "";

        //重複(CSV)チェック
        $dupFlg1 = $this->duplicateCsvCheck($duplicateCsv1, $primaryKey1, $data);
        if ($dupFlg1[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複1(". $dupFlg1[1] ."行目)");
        }
        $dupFlg2 = $this->duplicateCsvCheck($duplicateCsv2, $primaryKey2, $data);
        if ($dupFlg2[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複2(". $dupFlg2[1] ."行目)");
        }
        $dupFlg3 = $this->duplicateCsvCheck($duplicateCsv3, $primaryKey3, $data);
        if ($dupFlg3[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複3(". $dupFlg3[1] ."行目)");
        }

        //対象データが異なる場合、ループから抜ける。
        if ($this->field["CSVCD"] != $data["CSVCD"]) {
            if ($this->field["CSVCD"] == "1") {
                $returnFlg = $this->errSet($otherErrFlg, $otherErrMsg, "※その他:", $otherSep, "CSVCDが健康診断（一般）ではありません。");
            }
            if ($this->field["CSVCD"] == "2") {
                $returnFlg = $this->errSet($otherErrFlg, $otherErrMsg, "※その他:", $otherSep, "CSVCDが健康診断（歯・口腔）ではありません。");
            }
        }

        foreach ($checkdata as $key => $val) {
             //更新
            if ($this->field["SHORI_MEI"] == "1") {
                //一般-----
                if ($this->field["CSVCD"] == "1") {
                    //必須チェック
                    if (in_array($key, array("YEAR", "DATE", "SCHREGNO"))) {
                        if (!strlen(trim($data[$key]))) {
                            $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                        }
                    }

                    //数値チェック
                    if (
                        in_array($key, array("YEAR", "SCHREGNO", "R_VISION_CANTMEASURE", "L_VISION_CANTMEASURE", "R_BAREVISION_MARK", "L_BAREVISION_MARK", "R_VISION_MARK", "L_VISION_MARK", "R_EAR_CANTMEASURE", "R_EAR"
                        , "L_EAR_CANTMEASURE", "L_EAR", "NUTRITIONCD", "SPINERIBCD", "SPINERIBCD1", "SPINERIBCD2", "SPINERIBCD3", "EYEDISEASECD", "EYEDISEASECD5", "NOSEDISEASECD"
                        , "NOSEDISEASECD5", "NOSEDISEASECD6", "NOSEDISEASECD7", "SKINDISEASECD", "TB_FILMNO", "TB_REMARKCD", "TB_RE_EXAMINATION_RESULT", "TB_RE_EXAMINATION_FILMNO", "TB_OTHERTESTCD", "TB_NAMECD"
                        , "TB_ADVISECD", "HEART_MEDEXAM", "MANAGEMENT_DIV", "HEARTDISEASECD", "ALBUMINURIA1CD", "URICSUGAR1CD", "URICBLEED1CD", "ALBUMINURIA2CD", "URICSUGAR2CD", "URICBLEED2CD"
                        , "DETAILED_EXAMINATION", "URI_ADVISECD", "TREATCD", "TREATCD2", "MEDICAL_HISTORY1", "MEDICAL_HISTORY2", "MEDICAL_HISTORY3")) && $data[$key] != "" && !is_numeric($data[$key])
                    ) {
                        $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                    }

                    if (
                        in_array($key, array("HEIGHT", "WEIGHT", "R_BAREVISION", "L_BAREVISION", "R_VISION", "L_VISION", "R_EAR_DB", "L_EAR_DB")) && $data[$key] != ""
                        && !is_numeric(str_replace(".", "", $data[$key]))
                    ) {
                        $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                    }

                    //値チェック
                    if (in_array($key, array("HEIGHT", "WEIGHT", "R_EAR_DB", "L_EAR_DB"))) {
                        if ($data[$key] != "" && !$this->ataiCheck($data[$key], 3, 1)) {
                            $returnFlg = $this->errSet($valuesErrFlg, $valuesErrMsg, "※値不正:", $valuesSep, $val[1]);
                        }
                    }
                    if (in_array($key, array("R_BAREVISION", "L_BAREVISION", "R_VISION", "L_VISION"))) {
                        if ($data[$key] != "" && !$this->ataiCheck($data[$key], 1, 3)) {
                            $returnFlg = $this->errSet($valuesErrFlg, $valuesErrMsg, "※値不正:", $valuesSep, $val[1]);
                        }
                    }

                    if (in_array($key, array("R_VISION_CANTMEASURE", "L_VISION_CANTMEASURE", "R_EAR_CANTMEASURE", "L_EAR_CANTMEASURE"))) {
                        if ($data[$key] != "" && !$this->cantmeasureCheck($data[$key])) {
                            $returnFlg = $this->errSet($valuesErrFlg, $valuesErrMsg, "※値不正:", $valuesSep, $val[1]);
                        }
                    }
                    //文字数チェック
                    if ($data[$key] != "" && strlen($data[$key]) > $val[0]) {
                        $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                    }
                //歯・口腔-----
                } else {
                    //必須チェック
                    if (in_array($key, array("YEAR", "TOOTH_DATE", "SCHREGNO"))) {
                        if (!strlen(trim($data[$key]))) {
                            $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                        }
                    }
                    if (in_array($key, array("ORTHODONTICS"))) {
                        if (!in_array($data[$key], array("0","1"))) {
                            $this->errSet($valuesErrFlg, $valuesErrMsg, "※値不正:", $valuesSep, $val[1]);
                        }
                    }
                    //数値チェック
                    if (
                        in_array($key, array("YEAR", "SCHREGNO", "JAWS_JOINTCD", "JAWS_JOINTCD2", "PLAQUECD", "GUMCD", "CALCULUS", "ORTHODONTICS", "UP_R_BABY5", "UP_R_BABY4", "UP_R_BABY3"
                        , "UP_R_BABY2", "UP_R_BABY1", "UP_L_BABY1", "UP_L_BABY2", "UP_L_BABY3", "UP_L_BABY4", "UP_L_BABY5", "LW_R_BABY5", "LW_R_BABY4", "LW_R_BABY3", "LW_R_BABY2"
                        , "LW_R_BABY1", "LW_L_BABY1", "LW_L_BABY2", "LW_L_BABY3", "LW_L_BABY4", "LW_L_BABY5", "BABYTOOTH", "REMAINBABYTOOTH", "TREATEDBABYTOOTH", "BRACK_BABYTOOTH"
                        , "UP_R_ADULT8", "UP_R_ADULT7", "UP_R_ADULT6", "UP_R_ADULT5", "UP_R_ADULT4", "UP_R_ADULT3", "UP_R_ADULT2", "UP_R_ADULT1", "UP_L_ADULT1", "UP_L_ADULT2"
                        , "UP_L_ADULT3", "UP_L_ADULT4", "UP_L_ADULT5", "UP_L_ADULT6", "UP_L_ADULT7", "UP_L_ADULT8", "LW_R_ADULT8", "LW_R_ADULT7", "LW_R_ADULT6", "LW_R_ADULT5", "LW_R_ADULT4"
                        , "LW_R_ADULT3", "LW_R_ADULT2", "LW_R_ADULT1", "LW_L_ADULT1", "LW_L_ADULT2", "LW_L_ADULT3", "LW_L_ADULT4", "LW_L_ADULT5", "LW_L_ADULT6", "LW_L_ADULT7", "LW_L_ADULT8"
                        , "ADULTTOOTH", "REMAINADULTTOOTH", "TREATEDADULTTOOTH", "LOSTADULTTOOTH", "BRACK_ADULTTOOTH", "OTHERDISEASECD", "OTHERDISEASECD2", "DENTISTREMARKCD", "DENTISTREMARKCD2"
                        , "DENTISTREMARKCD3", "DENTISTTREATCD", "DENTISTTREATCD2"))
                    ) {
                        if ($data[$key] != "" && !is_numeric($data[$key])) {
                            $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                        }
                    }
                    //文字数チェック
                    if ($data[$key] != "" && strlen($data[$key]) > $val[0]) {
                        $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                    }
                }   //csvcd
            //削除
            } else {
            //一般-----
            //歯・口腔-----
            //桁数チェック
                //必須チェック
                if (in_array($key, array("YEAR", "DATE", "TOOTH_DATE", "SCHREGNO"))) {
                    if (!strlen(trim($data[$key]))) {
                        $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                    }
                }
                //数値チェック
                if (in_array($key, array("YEAR", "SCHREGNO"))) {
                    if ($data[$key] != "" && !is_numeric($data[$key])) {
                        $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                    }
                }
                //文字数チェック
                if (in_array($key, array("YEAR", "DATE", "TOOTH_DATE", "SCHREGNO"))) {
                    if (strlen($data[$key]) > $val[0]) {
                        $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                    }
                }
            }
        }

        if ($returnFlg) {
            foreach ($checkdata as $key => $val) {
                if ($this->field["CSVCD"] == "1") {
                    //日付の書式チェック
                    if (in_array($key, array("DATE", "TB_FILMDATE","TB_RE_EXAMINATION_DATE","DOC_DATE"))) {
                        if ($data[$key] != "" && !$this->dateRep($data[$key])) {
                            $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※日付:", $numericSep, $val[1]);
                        }
                    }
                //歯・口腔-----
                } else {
                    //日付の書式チェック
                    if (in_array($key, array("TOOTH_DATE", "DENTISTREMARKDATE"))) {
                        if ($data[$key] != "" && !$this->dateRep($data[$key])) {
                            $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※日付:", $numericSep, $val[1]);
                        }
                    }
                }

                if ($this->field["SHORI_MEI"] == "1" && $data[$key] != "" && $val[2] != "") {
                    $query = knjf140jQuery::getMasterCheck($this, $data, "1", $val[2], $data[$key]);
                    $mark = $db->getOne($query);
                    if ($mark < 1) {
                        $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, $val[1]."(".$val[2].")");
                    }
                }
            }
            //存在チェック(更新時のみ)
            if ($data["YEAR"] != "" && $data["SCHREGNO"] != "") {
                $query = knjf140jQuery::getMasterCheck($this, $data, "2");
                if ($db->getOne($query) < 1) {
                    $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, "在籍データ");
                }
            }
        }
        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$hissuErrMsg.$numericErrMsg.$valuesErrMsg.$masterErrMsg.$lengthErrMsg.$otherErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knjf140jQuery::insertQueryErr($db, $line, $checkError);
        }
        return $returnFlg;
    }

    //ＣＳＶ出力
    public function getDownloadModel()
    {
        //項目
        $header_err = array("MSGROW"            => "行番号",
                            "MSGREMARK"         => "メッセージ");

        if (in_array($this->Properties["printKenkouSindanIppan"], array("1", "2", "3"))) {
            $Ippan = $this->Properties["printKenkouSindanIppan"];
        } else {
            $Ippan = "0";
        }

        $header = $header_rei = array();
        if ($this->field["CSVCD"] == "1") { //一般
            //ヘッダー
            if ($this->field["OUTPUT"] == "5") {  //コード一覧出力
                $header = array(  "視力（記号）"
                                , "聴力（状態）"
                                , "栄養状態"
                                , "脊柱・胸郭・四肢（全般）"
                                , "脊柱・胸郭・四肢（疾患1～3）"
                                , "目の疾病及び異常"
                                , "色覚異常"
                                , "耳鼻咽頭疾患（全般）"
                                , "耳鼻咽頭疾患（疾患1～3）"
                                , "皮膚疾患"
                                , "結核（所見）"
                                , "結核（再検査）"
                                , "結核（その他の検査）"
                                , "結核（疾病及び異常）"
                                , "結核（指導区分）"
                                , "心臓　臨床医学的検査（心電図）"
                                , "心臓（精密検査）"
                                , "心臓（疾病及び異常）"
                                , "尿（蛋白）"
                                , "尿（糖）"
                                , "尿（潜血）"
                                , "尿（精密検査）"
                                , "尿（精密検査）指導区分"
                                , "事後措置1"
                                , "事後措置2"
                                , "既往歴"
                                );
            } else {
                //ヘッダー
                $header["CSVCD"]                            = "※CSVCD";
                $header["YEAR"]                             = "※年度";
                $header["DATE"]                             = "※健診実施日";
                $header["SCHREGNO"]                         = "※学籍番号";
                $header["GRADE"]                            = "学年";
                $header["HR_CLASS"]                         = "組";
                $header["ATTENDNO"]                         = "出席番号";
                $header["NAME"]                             = "氏名";
                $header["HEIGHT"]                           = "身長";
                $header["WEIGHT"]                           = "体重";
                $header["R_VISION_CANTMEASURE"]             = "視力(右)測定困難";
                $header["L_VISION_CANTMEASURE"]             = "視力(左)測定困難";
                $header["R_BAREVISION_MARK"]                = "視力右裸眼(記号)";
                $header["L_BAREVISION_MARK"]                = "視力左裸眼(記号)";
                $header["R_VISION_MARK"]                    = "視力右矯正(記号)";
                $header["L_VISION_MARK"]                    = "視力左矯正(記号)";
                $header["R_BAREVISION"]                     = "視力右裸眼(数字)";
                $header["L_BAREVISION"]                     = "視力左裸眼(数字)";
                $header["R_VISION"]                         = "視力右矯正(数字)";
                $header["L_VISION"]                         = "視力左矯正(数字)";
                $header["R_EAR_CANTMEASURE"]                = "聴力(右)測定困難";
                $header["R_EAR_DB"]                         = "聴力右平均dB";
                $header["R_EAR"]                            = "聴力右状態コード";
                $header["L_EAR_CANTMEASURE"]                = "聴力(左)測定困難";
                $header["L_EAR_DB"]                         = "聴力左平均dB";
                $header["L_EAR"]                            = "聴力左状態コード";
                $header["NUTRITIONCD"]                      = "栄養状態コード";
                $header["NUTRITIONCD_REMARK"]               = "栄養状態所見";
                $header["SPINERIBCD"]                       = "脊柱胸郭四肢（全般）コード";
                $header["SPINERIBCD_REMARK"]                = "脊柱胸郭四肢（全般）所見";
                $header["SPINERIBCD1"]                      = "脊柱胸郭四肢（疾患1）コード";
                $header["SPINERIBCD_REMARK1"]               = "脊柱胸郭四肢（疾患1）所見";
                $header["SPINERIBCD2"]                      = "脊柱胸郭四肢（疾患2）コード";
                $header["SPINERIBCD_REMARK2"]               = "脊柱胸郭四肢（疾患2）所見";
                $header["SPINERIBCD3"]                      = "脊柱胸郭四肢（疾患3）コード";
                $header["SPINERIBCD_REMARK3"]               = "脊柱胸郭四肢（疾患3）所見";
                $header["EYEDISEASECD"]                     = "目疾病及異常コード";
                $header["EYE_TEST_RESULT"]                  = "目の疾病及び異常所見";
                $header["EYEDISEASECD5"]                    = "色覚異常コード";
                $header["VISION_CANTMEASURE"]               = "色覚異常所見";
                $header["NOSEDISEASECD"]                    = "耳鼻咽頭疾患（全般）コード";
                $header["NOSEDISEASECD_REMARK"]             = "耳鼻咽頭疾患（全般）所見";
                $header["NOSEDISEASECD5"]                   = "耳鼻咽頭疾患（疾患1）コード";
                $header["NOSEDISEASECD_REMARK1"]            = "耳鼻咽頭疾患（疾患1）所見";
                $header["NOSEDISEASECD6"]                   = "耳鼻咽頭疾患（疾患2）コード";
                $header["NOSEDISEASECD_REMARK2"]            = "耳鼻咽頭疾患（疾患2）所見";
                $header["NOSEDISEASECD7"]                   = "耳鼻咽頭疾患（疾患3）コード";
                $header["NOSEDISEASECD_REMARK3"]            = "耳鼻咽頭疾患（疾患3）所見";
                $header["SKINDISEASECD"]                    = "皮膚疾患コード";
                $header["SKINDISEASECD_REMARK"]             = "皮膚疾患所見";
                $header["TB_FILMDATE"]                      = "結核撮影日付";
                $header["TB_FILMNO"]                        = "結核フィルム番号";
                $header["TB_REMARKCD"]                      = "結核所見コード";
                $header["TB_X_RAY"]                         = "結核所見";
                $header["TB_RE_EXAMINATION_DATE"]           = "結核再検査日付";
                $header["TB_RE_EXAMINATION_RESULT"]         = "結核再検査結果";
                $header["TB_RE_EXAMINATION_FILMNO"]         = "結核再検査フィルム番号";
                $header["TB_OTHERTESTCD"]                   = "結核その他検査コード";
                $header["TB_NAMECD"]                        = "結核疾病及び異常コード";
                $header["TB_NAME_REMARK1"]                  = "結核疾病及び異常所見";
                $header["TB_ADVISECD"]                      = "結核指導区分コード";
                $header["HEART_MEDEXAM"]                    = "心臓臨床医学的検査コード";
                $header["HEART_MEDEXAM_REMARK"]             = "心臓臨床医学的検査";
                $header["MANAGEMENT_DIV"]                   = "心臓精密検査コード";
                $header["MANAGEMENT_REMARK"]                = "心臓精密検査所見";
                $header["HEARTDISEASECD"]                   = "心臓疾病及異常コード";
                $header["HEARTDISEASECD_REMARK"]            = "心臓疾病及異常所見";
                $header["ALBUMINURIA1CD"]                   = "一次尿蛋白コード";
                $header["URICSUGAR1CD"]                     = "一次尿糖コード";
                $header["URICBLEED1CD"]                     = "一次尿潜血コード";
                $header["ALBUMINURIA2CD"]                   = "再検査尿蛋白コード";
                $header["URICSUGAR2CD"]                     = "再検査尿糖コード";
                $header["URICBLEED2CD"]                     = "再検査尿潜血コード";
                $header["DETAILED_EXAMINATION"]             = "尿精密検査コード";
                $header["URI_ADVISECD"]                     = "尿精密検査指導区分コード";
                $header["URICOTHERTEST"]                    = "尿精密検査所見";
                $header["OTHER_REMARK"]                     = "その他疾病及異常所見";
                $header["DOC_DATE"]                         = "学校医日付";
                $header["DOC_REMARK"]                       = "学校医所見";
                $header["TREATCD"]                          = "事後処置1コード";
                $header["TREAT_REMARK1"]                    = "事後処置1所見";
                $header["TREATCD2"]                         = "事後処置2コード";
                $header["TREATCD2_REMARK1"]                    = "事後処置2所見";
                $header["REMARK"]                           = "備考";
                $header["MEDICAL_HISTORY1"]                 = "既往症1";
                $header["MEDICAL_HISTORY2"]                 = "既往症2";
                $header["MEDICAL_HISTORY3"]                 = "既往症3";
                $header["DIAGNOSIS_NAME"]                   = "診断名";
                $header["LASTCOLUMN"]                       = "LASTCOLUMN";

                //ヘッダー例
                $nextMonthDate = new DateTime(CTRL_DATE);
                $nextMonthDate = $nextMonthDate->modify('+1 month')->format('Y-m-d');

                $header_rei["CSVCD"]                        = "1";
                $header_rei["YEAR"]                         = CTRL_YEAR;
                $header_rei["DATE"]                         = CTRL_DATE;
                $header_rei["SCHREGNO"]                     = CTRL_YEAR."0001";
                $header_rei["GRADE"]                        = "１年";
                $header_rei["HR_CLASS"]                     = "１組";
                $header_rei["ATTENDNO"]                     = "001";
                $header_rei["NAME"]                         = "アルプ太郎";
                $header_rei["HEIGHT"]                       = "165.5";
                $header_rei["WEIGHT"]                       = "80.5";
                $header_rei["R_VISION_CANTMEASURE"]         = "1";
                $header_rei["L_VISION_CANTMEASURE"]         = "1";
                $header_rei["R_BAREVISION_MARK"]            = "03";
                $header_rei["L_BAREVISION_MARK"]            = "02";
                $header_rei["R_VISION_MARK"]                = "01";
                $header_rei["L_VISION_MARK"]                = "01";
                $header_rei["R_BAREVISION"]                 = "0.5";
                $header_rei["L_BAREVISION"]                 = "0.8";
                $header_rei["R_VISION"]                     = "1.0";
                $header_rei["L_VISION"]                     = "1.0";
                $header_rei["R_EAR_CANTMEASURE"]            = "1";
                $header_rei["R_EAR_DB"]                     = "20";
                $header_rei["R_EAR"]                        = "01";
                $header_rei["L_EAR_CANTMEASURE"]            = "1";
                $header_rei["L_EAR_DB"]                     = "15";
                $header_rei["L_EAR"]                        = "02";
                $header_rei["NUTRITIONCD"]                  = "01";
                $header_rei["NUTRITIONCD_REMARK"]           = "栄養状態所見";
                $header_rei["SPINERIBCD"]                   = "01";
                $header_rei["SPINERIBCD_REMARK"]            = "脊柱・胸郭・四肢所見";
                $header_rei["SPINERIBCD1"]                  = "01";
                $header_rei["SPINERIBCD_REMARK1"]           = "脊柱胸郭四肢（疾患1）所見";
                $header_rei["SPINERIBCD2"]                  = "02";
                $header_rei["SPINERIBCD_REMARK2"]           = "脊柱胸郭四肢（疾患2）所見";
                $header_rei["SPINERIBCD3"]                  = "03";
                $header_rei["SPINERIBCD_REMARK3"]           = "脊柱胸郭四肢（疾患3）所見";
                $header_rei["EYEDISEASECD"]                 = "01";
                $header_rei["EYE_TEST_RESULT"]              = "目の疾病及び異常所見";
                $header_rei["EYEDISEASECD5"]                = "01";
                $header_rei["VISION_CANTMEASURE"]           = "色覚異常所見";
                $header_rei["NOSEDISEASECD"]                = "01";
                $header_rei["NOSEDISEASECD_REMARK"]         = "耳鼻咽頭疾患（全般）所見";
                $header_rei["NOSEDISEASECD5"]               = "01";
                $header_rei["NOSEDISEASECD_REMARK1"]        = "耳鼻咽頭疾患（疾患1）所見";
                $header_rei["NOSEDISEASECD6"]               = "02";
                $header_rei["NOSEDISEASECD_REMARK2"]        = "耳鼻咽頭疾患（疾患2）所見";
                $header_rei["NOSEDISEASECD7"]               = "03";
                $header_rei["NOSEDISEASECD_REMARK3"]        = "耳鼻咽頭疾患（疾患3）所見";
                $header_rei["SKINDISEASECD"]                = "01";
                $header_rei["SKINDISEASECD_REMARK"]         = "皮膚疾患所見";
                $header_rei["TB_FILMDATE"]                  = CTRL_DATE;
                $header_rei["TB_FILMNO"]                    = "123456";
                $header_rei["TB_REMARKCD"]                  = "01";
                $header_rei["TB_X_RAY"]                     = "結核所見";
                $header_rei["TB_RE_EXAMINATION_DATE"]       = $nextMonthDate;
                $header_rei["TB_RE_EXAMINATION_RESULT"]     = "01";
                $header_rei["TB_RE_EXAMINATION_FILMNO"]     = "123456";
                $header_rei["TB_OTHERTESTCD"]               = "01";
                $header_rei["TB_NAMECD"]                    = "01";
                $header_rei["TB_NAME_REMARK1"]              = "結核疾病及び異常所見";
                $header_rei["TB_ADVISECD"]                  = "01";
                $header_rei["HEART_MEDEXAM"]                = "01";
                $header_rei["HEART_MEDEXAM_REMARK"]         = "不完全右脚ブロック、心雑音、不整脈など";
                $header_rei["MANAGEMENT_DIV"]               = "01";
                $header_rei["MANAGEMENT_REMARK"]            = "心臓精密検査所見";
                $header_rei["HEARTDISEASECD"]               = "01";
                $header_rei["HEARTDISEASECD_REMARK"]        = "疾病及び異常";
                $header_rei["ALBUMINURIA1CD"]               = "01";
                $header_rei["URICSUGAR1CD"]                 = "01";
                $header_rei["URICBLEED1CD"]                 = "01";
                $header_rei["ALBUMINURIA2CD"]               = "01";
                $header_rei["URICSUGAR2CD"]                 = "01";
                $header_rei["URICBLEED2CD"]                 = "01";
                $header_rei["DETAILED_EXAMINATION"]         = "01";
                $header_rei["URI_ADVISECD"]                 = "01";
                $header_rei["URICOTHERTEST"]                = "再検査済　異常なし";
                $header_rei["OTHER_REMARK"]                 = "その他疾病及異常所見";
                $header_rei["DOC_DATE"]                     = CTRL_DATE;
                $header_rei["DOC_REMARK"]                   = "食事療法";
                $header_rei["TREATCD"]                      = "01";
                $header_rei["TREAT_REMARK1"]                = "事後処置1所見";
                $header_rei["TREATCD2"]                     = "02";
                $header_rei["TREATCD2_REMARK1"]             = "事後処置2所見";
                $header_rei["REMARK"]                       = "食事療法";
                $header_rei["MEDICAL_HISTORY1"]             = "01";
                $header_rei["MEDICAL_HISTORY2"]             = "02";
                $header_rei["MEDICAL_HISTORY3"]             = "03";
                $header_rei["DIAGNOSIS_NAME"]               = "診断名";
                $header_rei["LASTCOLUMN"]                   = "LASTCOLUMN";
            }
        } else { //歯・口腔
            //ヘッダー
            if ($this->field["OUTPUT"] == "5") {  //コード一覧出力
                $header = array(  "顎関節"
                                , "歯列・咬合"
                                , "歯垢の状態"
                                , "歯肉の状態"
                                , "歯石沈着"
                                , "その他疾病及び異常"
                                , "口腔の疾病及び異常"
                                , "所見"
                                , "事後措置"
                                , "歯式"
                                );
            } else {
                $header["CSVCD"]                          = "※CSVCD";
                $header["YEAR"]                           = "※年度";
                $header["TOOTH_DATE"]                     = "※健診実施日";
                $header["SCHREGNO"]                       = "※学籍番号";
                $header["GRADE"]                          = "学年";
                $header["HR_CLASS"]                       = "組";
                $header["ATTENDNO"]                       = "出席番号";
                $header["NAME"]                           = "氏名";
                $header["JAWS_JOINTCD2"]                  = "顎関節コード";
                $header["JAWS_JOINTCD"]                   = "歯列・咬合コード";
                $header["PLAQUECD"]                       = "歯垢状態コード";
                $header["GUMCD"]                          = "歯肉状態コード";
                $header["CALCULUS"]                       = "歯石沈着";
                $header["ORTHODONTICS"]                   = "矯正の有無";
                $header["UP_R_BABY5"]                     = "乳歯 上 右E";
                $header["UP_R_BABY4"]                     = "乳歯 上 右D";
                $header["UP_R_BABY3"]                     = "乳歯 上 右C";
                $header["UP_R_BABY2"]                     = "乳歯 上 右B";
                $header["UP_R_BABY1"]                     = "乳歯 上 右A";
                $header["UP_L_BABY1"]                     = "乳歯 上 左A";
                $header["UP_L_BABY2"]                     = "乳歯 上 左B";
                $header["UP_L_BABY3"]                     = "乳歯 上 左C";
                $header["UP_L_BABY4"]                     = "乳歯 上 左D";
                $header["UP_L_BABY5"]                     = "乳歯 上 左E";
                $header["LW_R_BABY5"]                     = "乳歯 下 右E";
                $header["LW_R_BABY4"]                     = "乳歯 下 右D";
                $header["LW_R_BABY3"]                     = "乳歯 下 右C";
                $header["LW_R_BABY2"]                     = "乳歯 下 右B";
                $header["LW_R_BABY1"]                     = "乳歯 下 右A";
                $header["LW_L_BABY1"]                     = "乳歯 下 左A";
                $header["LW_L_BABY2"]                     = "乳歯 下 左B";
                $header["LW_L_BABY3"]                     = "乳歯 下 左C";
                $header["LW_L_BABY4"]                     = "乳歯 下 左D";
                $header["LW_L_BABY5"]                     = "乳歯 下 左E";
                $header["BABYTOOTH"]                      = "乳歯現在数";
                $header["REMAINBABYTOOTH"]                = "乳歯未処置数";
                $header["TREATEDBABYTOOTH"]               = "乳歯処置数";
                $header["BRACK_BABYTOOTH"]                = "要注意乳歯数";
                $header["UP_R_ADULT8"]                    = "永久歯 上 右8";
                $header["UP_R_ADULT7"]                    = "永久歯 上 右7";
                $header["UP_R_ADULT6"]                    = "永久歯 上 右6";
                $header["UP_R_ADULT5"]                    = "永久歯 上 右5";
                $header["UP_R_ADULT4"]                    = "永久歯 上 右4";
                $header["UP_R_ADULT3"]                    = "永久歯 上 右3";
                $header["UP_R_ADULT2"]                    = "永久歯 上 右2";
                $header["UP_R_ADULT1"]                    = "永久歯 上 右1";
                $header["UP_L_ADULT1"]                    = "永久歯 上 左1";
                $header["UP_L_ADULT2"]                    = "永久歯 上 左2";
                $header["UP_L_ADULT3"]                    = "永久歯 上 左3";
                $header["UP_L_ADULT4"]                    = "永久歯 上 左4";
                $header["UP_L_ADULT5"]                    = "永久歯 上 左5";
                $header["UP_L_ADULT6"]                    = "永久歯 上 左6";
                $header["UP_L_ADULT7"]                    = "永久歯 上 左7";
                $header["UP_L_ADULT8"]                    = "永久歯 上 左8";
                $header["LW_R_ADULT8"]                    = "永久歯 下 右8";
                $header["LW_R_ADULT7"]                    = "永久歯 下 右7";
                $header["LW_R_ADULT6"]                    = "永久歯 下 右6";
                $header["LW_R_ADULT5"]                    = "永久歯 下 右5";
                $header["LW_R_ADULT4"]                    = "永久歯 下 右4";
                $header["LW_R_ADULT3"]                    = "永久歯 下 右3";
                $header["LW_R_ADULT2"]                    = "永久歯 下 右2";
                $header["LW_R_ADULT1"]                    = "永久歯 下 右1";
                $header["LW_L_ADULT1"]                    = "永久歯 下 左1";
                $header["LW_L_ADULT2"]                    = "永久歯 下 左2";
                $header["LW_L_ADULT3"]                    = "永久歯 下 左3";
                $header["LW_L_ADULT4"]                    = "永久歯 下 左4";
                $header["LW_L_ADULT5"]                    = "永久歯 下 左5";
                $header["LW_L_ADULT6"]                    = "永久歯 下 左6";
                $header["LW_L_ADULT7"]                    = "永久歯 下 左7";
                $header["LW_L_ADULT8"]                    = "永久歯 下 左8";
                $header["ADULTTOOTH"]                     = "永久歯数";
                $header["REMAINADULTTOOTH"]               = "永久歯未処置数";
                $header["TREATEDADULTTOOTH"]              = "永久歯処置数";
                $header["LOSTADULTTOOTH"]                 = "永久歯喪失数";
                $header["BRACK_ADULTTOOTH"]               = "要観察歯数";
                $header["OTHERDISEASECD"]                 = "その他疾病及び異常コード";
                $header["OTHERDISEASE"]                   = "その他疾病及び異常";
                $header["OTHERDISEASECD2"]                = "口腔の疾病及び異常コード";
                $header["OTHERDISEASE2"]                  = "口腔の疾病及び異常";
                $header["DENTISTREMARKCD"]                = "学校歯科医所見コード1";
                $header["DENTISTREMARK"]                  = "学校歯科医所見1";
                $header["DENTISTREMARKCD2"]               = "学校歯科医所見コード2";
                $header["DENTISTREMARK2"]                 = "学校歯科医所見2";
                $header["DENTISTREMARKCD3"]               = "学校歯科医所見コード3";
                $header["DENTISTREMARK3"]                 = "学校歯科医所見3";
                $header["DENTISTREMARKDATE"]              = "学校歯科医所見日付";
                $header["DENTISTTREATCD"]                 = "学校歯科医事後処置コード1";
                $header["DENTISTTREAT"]                   = "学校歯科医事後処置1";
                $header["DENTISTTREATCD2"]                = "学校歯科医事後処置コード2";
                $header["DENTISTTREAT2"]                  = "学校歯科医事後処置2";
                $header["LASTCOLUMN"]                     = "LASTCOLUMN";

                //ヘッダー例
                $header_rei["CSVCD"]                      = "2";
                $header_rei["YEAR"]                       = CTRL_YEAR;
                $header_rei["TOOTH_DATE"]                 = CTRL_DATE;
                $header_rei["SCHREGNO"]                   = CTRL_YEAR."0001";
                $header_rei["GRADE"]                      = "１年";
                $header_rei["HR_CLASS"]                   = "１組";
                $header_rei["ATTENDNO"]                   = "001";
                $header_rei["NAME"]                       = "アルプ太郎";
                $header_rei["JAWS_JOINTCD2"]              = "01";
                $header_rei["JAWS_JOINTCD"]               = "01";
                $header_rei["PLAQUECD"]                   = "02";
                $header_rei["GUMCD"]                      = "01";
                $header_rei["CALCULUS"]                   = "01";
                $header_rei["ORTHODONTICS"]               = "1";
                $header_rei["UP_R_BABY5"]                 = "01";
                $header_rei["UP_R_BABY4"]                 = "02";
                $header_rei["UP_R_BABY3"]                 = "03";
                $header_rei["UP_R_BABY2"]                 = "04";
                $header_rei["UP_R_BABY1"]                 = "05";
                $header_rei["UP_L_BABY1"]                 = "06";
                $header_rei["UP_L_BABY2"]                 = "01";
                $header_rei["UP_L_BABY3"]                 = "02";
                $header_rei["UP_L_BABY4"]                 = "03";
                $header_rei["UP_L_BABY5"]                 = "04";
                $header_rei["LW_R_BABY5"]                 = "05";
                $header_rei["LW_R_BABY4"]                 = "06";
                $header_rei["LW_R_BABY3"]                 = "01";
                $header_rei["LW_R_BABY2"]                 = "02";
                $header_rei["LW_R_BABY1"]                 = "03";
                $header_rei["LW_L_BABY1"]                 = "04";
                $header_rei["LW_L_BABY2"]                 = "05";
                $header_rei["LW_L_BABY3"]                 = "06";
                $header_rei["LW_L_BABY4"]                 = "01";
                $header_rei["LW_L_BABY5"]                 = "02";
                $header_rei["BABYTOOTH"]                  = "8";
                $header_rei["REMAINBABYTOOTH"]            = "1";
                $header_rei["TREATEDBABYTOOTH"]           = "2";
                $header_rei["BRACK_BABYTOOTH"]            = "5";
                $header_rei["UP_R_ADULT8"]                = "01";
                $header_rei["UP_R_ADULT7"]                = "02";
                $header_rei["UP_R_ADULT6"]                = "03";
                $header_rei["UP_R_ADULT5"]                = "04";
                $header_rei["UP_R_ADULT4"]                = "05";
                $header_rei["UP_R_ADULT3"]                = "06";
                $header_rei["UP_R_ADULT2"]                = "01";
                $header_rei["UP_R_ADULT1"]                = "02";
                $header_rei["UP_L_ADULT1"]                = "03";
                $header_rei["UP_L_ADULT2"]                = "04";
                $header_rei["UP_L_ADULT3"]                = "05";
                $header_rei["UP_L_ADULT4"]                = "06";
                $header_rei["UP_L_ADULT5"]                = "01";
                $header_rei["UP_L_ADULT6"]                = "02";
                $header_rei["UP_L_ADULT7"]                = "03";
                $header_rei["UP_L_ADULT8"]                = "04";
                $header_rei["LW_R_ADULT8"]                = "05";
                $header_rei["LW_R_ADULT7"]                = "06";
                $header_rei["LW_R_ADULT6"]                = "01";
                $header_rei["LW_R_ADULT5"]                = "02";
                $header_rei["LW_R_ADULT4"]                = "03";
                $header_rei["LW_R_ADULT3"]                = "04";
                $header_rei["LW_R_ADULT2"]                = "05";
                $header_rei["LW_R_ADULT1"]                = "06";
                $header_rei["LW_L_ADULT1"]                = "01";
                $header_rei["LW_L_ADULT2"]                = "02";
                $header_rei["LW_L_ADULT3"]                = "03";
                $header_rei["LW_L_ADULT4"]                = "04";
                $header_rei["LW_L_ADULT5"]                = "05";
                $header_rei["LW_L_ADULT6"]                = "06";
                $header_rei["LW_L_ADULT7"]                = "01";
                $header_rei["LW_L_ADULT8"]                = "02";
                $header_rei["ADULTTOOTH"]                 = "20";
                $header_rei["REMAINADULTTOOTH"]           = "3";
                $header_rei["TREATEDADULTTOOTH"]          = "5";
                $header_rei["LOSTADULTTOOTH"]             = "2";
                $header_rei["BRACK_ADULTTOOTH"]           = "3";
                $header_rei["OTHERDISEASECD"]             = "99";
                $header_rei["OTHERDISEASE"]               = "全角20文字";
                $header_rei["OTHERDISEASECD2"]            = "99";
                $header_rei["OTHERDISEASE2"]              = "全角20文字";
                $header_rei["DENTISTREMARKCD"]            = "01";
                $header_rei["DENTISTREMARK"]              = "全角20文字";
                $header_rei["DENTISTREMARKCD2"]           = "02";
                $header_rei["DENTISTREMARK2"]             = "全角20文字";
                $header_rei["DENTISTREMARKCD3"]           = "03";
                $header_rei["DENTISTREMARK3"]             = "全角20文字";
                $header_rei["DENTISTREMARKDATE"]          = CTRL_DATE;
                $header_rei["DENTISTTREATCD"]             = "02";
                $header_rei["DENTISTTREAT"]               = "全角20文字";
                $header_rei["DENTISTTREATCD2"]            = "03";
                $header_rei["DENTISTTREAT2"]              = "全角20文字";
                $header_rei["LASTCOLUMN"]                 = "LASTCOLUMN";
            }
        }
        $contents  = ""; //クリア

        if ($this->field["OUTPUT"] == "1") { //ヘッダ
            if ($this->field["HEADER"] == "on") {
                $contents .= implode($header, ",")."\n";
            }
            $sep = "";
            foreach ($header as $key => $val) {
                $contents .= $sep.$header_rei[$key];
                $sep = ",";
            }
            $contents .= "\n";
        } elseif ($this->field["OUTPUT"] == "3") {        //エラー
            $headerDuplicate = array("YEAR"      => "年度",
                                     "SCHREGNO"  => "学籍番号",
                                     "TOOTH_SEQ" => "健康診断SEQ",
                                     "DET_SEQ"   => "健康診断SEQ");
            $tables = array();
            if ($this->field["CSVCD"] == "1") {
                $tables = array("1" => "MEDEXAM_DET_DAT", "2" => "MEDEXAM_DET_DETAIL_DAT", "3" => "MEDEXAM_HDAT");
            } else {
                $tables = array("1" => "MEDEXAM_TOOTH_DAT", "2" => "MEDEXAM_TOOTH_DETAIL_DAT", "3" => "MEDEXAM_HDAT");
            }
            $dupTable = $this->getDuplicateErrorHeader(PROGRAMID, $tables);
            if (sizeof($dupTable) > 0) {
                $headerErr = $this->duplicateCsvHeader($dupTable, $headerDuplicate);
                $contents .= implode($headerErr, ",")."\n";
            }
            $contents .= implode($header_err, ",")."\n";
        } elseif ($this->field["OUTPUT"] == "4") {        //データ
            if ($this->field["HEADER"] == "on") {
                $contents .= implode($header, ",")."\n";
            }
        } elseif ($this->field["OUTPUT"] == "5") {        //コード一覧
            if ($this->field["HEADER"] == "on") {
                for ($head = 0; $head < get_count($header); $head++) {
                    $contents .= "{$header[$head]}".",,,";
                }
                $contents .= "\n";
            }
        }

        if ($this->field["OUTPUT"] == "1") { //ヘッダ
            $header_data = true;
        } elseif ($this->field["OUTPUT"] == "5") { //コード一覧出力
            $header_data = false;

            if ($this->field["CSVCD"] == "1") { //一般
                $namecd1  = array(  "'F017'"    //視力（記号）
                                  , "'F010'"    //聴力（状態）
                                  , "'F030'"    //栄養状態
                                  , "'F040'"    //脊柱・胸郭・四肢（全般）
                                  , "'F041'"    //脊柱・胸郭・四肢（疾患1～3）
                                  , "'F050'"    //目の疾病及び異常
                                  , "'F051'"    //色覚異常
                                  , "'F060'"    //耳鼻咽頭疾患（全般）
                                  , "'F061'"    //耳鼻咽頭疾患（疾患1～3）
                                  , "'F070'"    //皮膚疾患
                                  , "'F100'"    //結核（所見）
                                  , "'F101'"    //結核（再検査）
                                  , "'F110'"    //結核（その他の検査）
                                  , "'F120'"    //結核（疾病及び異常）
                                  , "'F130'"    //結核（指導区分）
                                  , "'F080'"    //心臓　臨床医学的検査（心電図）
                                  , "'F091'"    //心臓（精密検査）
                                  , "'F090'"    //心臓（疾病及び異常）
                                  , "'F020'"    //尿（蛋白）
                                  , "'F019'"    //尿（糖）
                                  , "'F018'"    //尿（潜血）
                                  , "'F146'"    //尿（精密検査）
                                  , "'F021'"    //尿（精密検査）指導区分
                                  , "'F150'"    //事後措置1
                                  , "'F151'"    //事後措置2
                                  , "'F143'"    //既往歴
                                  );
            } else {  //歯・口腔
                $namecd1  = array(  "'F511'"    //顎関節
                                  , "'F510'"    //歯列・咬合
                                  , "'F520'"    //歯垢の状態
                                  , "'F513'"    //歯肉の状態
                                  , "'F521'"    //歯石沈着
                                  , "'F530'"    //その他疾病及び異常
                                  , "'F531'"    //口腔の疾病及び異常
                                  , "'F540'"    //所見
                                  , "'F541'"    //事後措置
                                  , "'F550'"    //歯式
                                  );
            }
            $nameList = array();
            $maxRow = 0;

            //CSV2行目
            if ($this->field["HEADER"] == "on") {
                for ($count = 0; $count < get_count($namecd1); $count++) {
                    $contents .= "コード,名称,,";
                }
                $contents .= "\n";
            }

            //DB接続
            $db = Query::dbCheckOut();

            $query = knjf140jQuery::getMaxRowCount(implode($namecd1, ","));

            //SQL文発行
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $maxRow = $row["ROW_COUNT"];
            }
            $result->free();

            for ($count = 0; $count < get_count($namecd1); $count++) {
                $query = knjf140jQuery::getCodeListSQL($namecd1[$count]);

                //SQL文発行
                $result = $db->query($query);
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $row = str_replace(",", ".", $row);//データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                    $nameList[$count][]   = $row["CD"].",".$row["NAME"].",,";
                    $header_data = true;
                }
                $result->free();
            }

            for ($row = 0; $row < $maxRow; $row++) {
                for ($col = 0; $col < get_count($namecd1); $col++) {
                    if (empty($nameList[$col][$row])) {
                        $contents .= ",,,";    //配列に値が入っていない場合空白を入れる
                    } else {
                        $contents .= $nameList[$col][$row];
                    }
                }
                $contents .= "\n";
            }

            Query::dbCheckIn($db);
        } else { //ヘッダ以外
            $header_data = false;
            //DB接続
            $db = Query::dbCheckOut();
            if ($this->field["OUTPUT"] == "3") { //エラー
                $query = $this->selectCsvErrQuery(PROGRAMID);
            } elseif ($this->field["OUTPUT"] == "4") { //データ
                $query = knjf140jQuery::getselectDataSQL($this);
            } elseif ($this->field["OUTPUT"] == "5") { //コード一覧
                $query = knjf140jQuery::getselectDataSQL($this);
            }
            //SQL文発行
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($this->field["OUTPUT"] == "4") { //データ
                    $row = str_replace(",", ".", $row);//データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。

                    $sep = "";
                    foreach ($header as $key => $val) {
                        $contents .= $sep.$row[$key];
                        $sep = ",";
                    }
                    $contents .= "\n";
                } else {
                    $contents .= implode($row, ",") ."\n";
                }
                $header_data = true;
            }
            $result->free();
            Query::dbCheckIn($db);
        }
        //ＣＳＶ出力
        if ($header_data) {
            if ($this->field["OUTPUT"] == "3") {        //エラー
                $couse = "エラー出力";
            } elseif ($this->field["OUTPUT"] == "5") {  //コード一覧出力
                $couse = "健康診断（一般）コード一覧";
            } else {                                  //エラー以外
                $couse = ($this->field["CSVCD"] == "1") ? "健康診断（一般）" : "健康診断（歯・口腔）";
            }
            $filename = sprintf("%s.csv", $couse);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }

    //値チェック
    public function ataiCheck($data, $atai1, $atai2)
    {
        $flg = true;

        if ($data != "") {
            $data_arr = array();
            $data_arr = explode(".", $data);
            if (strlen($data_arr[0]) > $atai1) {
                $flg = false;
            }
            if (strlen($data_arr[1]) > $atai2) {
                $flg = false;
            }
        }

        return $flg;
    }

    //測定困難の値チェック
    public function cantmeasureCheck($data)
    {
        $flg = true;

        if ($data != "1" && $data != "") {
            $flg = false;
        }
        return $flg;
    }

    /**
     * 日付変換 項目ﾁｪｯｸ後に行う。
     */
    public function dateRep($data)
    {
        if ($data != "") {
            $data = common::DateConv1(str_replace("-", "/", $data), 4);
            if ($data) {
                $data = str_replace("/", "-", $data);
            }
        }
        return $data;
    }

    /**
     * ０埋め 項目ﾁｪｯｸ後に行う。
     */
    public function zeroUme($data)
    {
        //更新
        if ($this->field["SHORI_MEI"] == "1") {
            if ($data["SCHREGNO"] != "") {
                if (strlen($data["SCHREGNO"]) < 8 && is_numeric($data["SCHREGNO"])) {
                    $data["SCHREGNO"] = sprintf("%08d", $data["SCHREGNO"]);
                }
            }

            //一般-----
            if ($this->field["CSVCD"] == "1") {
                if ($data["R_BAREVISION_MARK"] != "") {
                    if (strlen($data["R_BAREVISION_MARK"]) < 2 && is_numeric($data["R_BAREVISION_MARK"])) {
                        $data["R_BAREVISION_MARK"]   = sprintf("%02d", $data["R_BAREVISION_MARK"]);
                    }
                }
                if ($data["L_BAREVISION_MARK"] != "") {
                    if (strlen($data["L_BAREVISION_MARK"]) < 2 && is_numeric($data["L_BAREVISION_MARK"])) {
                        $data["L_BAREVISION_MARK"]   = sprintf("%02d", $data["L_BAREVISION_MARK"]);
                    }
                }
                if ($data["R_VISION_MARK"] != "") {
                    if (strlen($data["R_VISION_MARK"]) < 2 && is_numeric($data["R_VISION_MARK"])) {
                        $data["R_VISION_MARK"]       = sprintf("%02d", $data["R_VISION_MARK"]);
                    }
                }
                if ($data["L_VISION_MARK"] != "") {
                    if (strlen($data["L_VISION_MARK"]) < 2 && is_numeric($data["L_VISION_MARK"])) {
                        $data["L_VISION_MARK"]       = sprintf("%02d", $data["L_VISION_MARK"]);
                    }
                }
                if ($data["R_BAREVISION"] != "") {
                    $data_arr = array();
                    $data_arr = explode(".", $data["R_BAREVISION"]);
                    if (is_numeric($data_arr[0]) && is_numeric($data_arr[1])) {
                        $data["R_BAREVISION"] = $data_arr[0] . "." . sprintf("%-03s", $data_arr[1]);
                    }
                }
                if ($data["L_BAREVISION"] != "") {
                    $data_arr = array();
                    $data_arr = explode(".", $data["L_BAREVISION"]);
                    if (is_numeric($data_arr[0]) && is_numeric($data_arr[1])) {
                        $data["L_BAREVISION"] = $data_arr[0] . "." . sprintf("%-03s", $data_arr[1]);
                    }
                }
                if ($data["R_VISION"] != "") {
                    $data_arr = array();
                    $data_arr = explode(".", $data["R_VISION"]);
                    if (is_numeric($data_arr[0]) && is_numeric($data_arr[1])) {
                        $data["R_VISION"] = $data_arr[0] . "." . sprintf("%-03s", $data_arr[1]);
                    }
                }
                if ($data["L_VISION"] != "") {
                    $data_arr = array();
                    $data_arr = explode(".", $data["L_VISION"]);
                    if (is_numeric($data_arr[0]) && is_numeric($data_arr[1])) {
                        $data["L_VISION"] = $data_arr[0] . "." . sprintf("%-03s", $data_arr[1]);
                    }
                }
                if ($data["R_EAR_DB"] != "") {
                    $data_arr = array();
                    $data_arr = explode(".", $data["R_EAR_DB"]);
                    if (is_numeric($data_arr[0]) && is_numeric($data_arr[1])) {
                        $data["R_EAR_DB"] = $data_arr[0] . "." . sprintf("%-01s", $data_arr[1]);
                    }
                }
                if ($data["R_EAR"] != "") {
                    if (strlen($data["R_EAR"]) < 2 && is_numeric($data["R_EAR"])) {
                        $data["R_EAR"]               = sprintf("%02d", $data["R_EAR"]);
                    }
                }
                if ($data["L_EAR_DB"] != "") {
                    $data_arr = array();
                    $data_arr = explode(".", $data["L_EAR_DB"]);
                    if (is_numeric($data_arr[0]) && is_numeric($data_arr[1])) {
                        $data["L_EAR_DB"] = $data_arr[0] . "." . sprintf("%-01s", $data_arr[1]);
                    }
                }
                if ($data["L_EAR"] != "") {
                    if (strlen($data["L_EAR"]) < 2 && is_numeric($data["L_EAR"])) {
                        $data["L_EAR"]               = sprintf("%02d", $data["L_EAR"]);
                    }
                }
                if ($data["NUTRITIONCD"] != "") {
                    if (strlen($data["NUTRITIONCD"]) < 2 && is_numeric($data["NUTRITIONCD"])) {
                        $data["NUTRITIONCD"]         = sprintf("%02d", $data["NUTRITIONCD"]);
                    }
                }
                if ($data["SPINERIBCD"] != "") {
                    if (strlen($data["SPINERIBCD"]) < 2 && is_numeric($data["SPINERIBCD"])) {
                        $data["SPINERIBCD"]          = sprintf("%02d", $data["SPINERIBCD"]);
                    }
                }
                if ($data["SPINERIBCD1"] != "") {
                    if (strlen($data["SPINERIBCD1"]) < 2 && is_numeric($data["SPINERIBCD1"])) {
                        $data["SPINERIBCD1"]         = sprintf("%02d", $data["SPINERIBCD1"]);
                    }
                }
                if ($data["SPINERIBCD2"] != "") {
                    if (strlen($data["SPINERIBCD2"]) < 2 && is_numeric($data["SPINERIBCD2"])) {
                        $data["SPINERIBCD2"]         = sprintf("%02d", $data["SPINERIBCD2"]);
                    }
                }
                if ($data["SPINERIBCD3"] != "") {
                    if (strlen($data["SPINERIBCD3"]) < 2 && is_numeric($data["SPINERIBCD3"])) {
                        $data["SPINERIBCD3"]         = sprintf("%02d", $data["SPINERIBCD3"]);
                    }
                }
                if ($data["EYEDISEASECD"] != "") {
                    if (strlen($data["EYEDISEASECD"]) < 2 && is_numeric($data["EYEDISEASECD"])) {
                        $data["EYEDISEASECD"]        = sprintf("%02d", $data["EYEDISEASECD"]);
                    }
                }
                if ($data["EYEDISEASECD5"] != "") {
                    if (strlen($data["EYEDISEASECD5"]) < 2 && is_numeric($data["EYEDISEASECD5"])) {
                        $data["EYEDISEASECD5"]       = sprintf("%02d", $data["EYEDISEASECD5"]);
                    }
                }
                if ($data["NOSEDISEASECD"] != "") {
                    if (strlen($data["NOSEDISEASECD"]) < 2 && is_numeric($data["NOSEDISEASECD"])) {
                        $data["NOSEDISEASECD"]       = sprintf("%02d", $data["NOSEDISEASECD"]);
                    }
                }
                if ($data["NOSEDISEASECD5"] != "") {
                    if (strlen($data["NOSEDISEASECD5"]) < 2 && is_numeric($data["NOSEDISEASECD5"])) {
                        $data["NOSEDISEASECD5"]      = sprintf("%02d", $data["NOSEDISEASECD5"]);
                    }
                }
                if ($data["NOSEDISEASECD6"] != "") {
                    if (strlen($data["NOSEDISEASECD6"]) < 2 && is_numeric($data["NOSEDISEASECD6"])) {
                        $data["NOSEDISEASECD6"]      = sprintf("%02d", $data["NOSEDISEASECD6"]);
                    }
                }
                if ($data["NOSEDISEASECD7"] != "") {
                    if (strlen($data["NOSEDISEASECD7"]) < 2 && is_numeric($data["NOSEDISEASECD7"])) {
                        $data["NOSEDISEASECD7"]      = sprintf("%02d", $data["NOSEDISEASECD7"]);
                    }
                }
                if ($data["SKINDISEASECD"] != "") {
                    if (strlen($data["SKINDISEASECD"]) < 2 && is_numeric($data["SKINDISEASECD"])) {
                        $data["SKINDISEASECD"]       = sprintf("%02d", $data["SKINDISEASECD"]);
                    }
                }
                if ($data["TB_REMARKCD"] != "") {
                    if (strlen($data["TB_REMARKCD"]) < 2 && is_numeric($data["TB_REMARKCD"])) {
                        $data["TB_REMARKCD"]         = sprintf("%02d", $data["TB_REMARKCD"]);
                    }
                }
                if ($data["TB_RE_EXAMINATION_RESULT"] != "") {
                    if (strlen($data["TB_RE_EXAMINATION_RESULT"]) < 2 && is_numeric($data["TB_RE_EXAMINATION_RESULT"])) {
                        $data["TB_RE_EXAMINATION_RESULT"] = sprintf("%02d", $data["TB_RE_EXAMINATION_RESULT"]);
                    }
                }
                if ($data["TB_OTHERTESTCD"] != "") {
                    if (strlen($data["TB_OTHERTESTCD"]) < 2 && is_numeric($data["TB_OTHERTESTCD"])) {
                        $data["TB_OTHERTESTCD"]      = sprintf("%02d", $data["TB_OTHERTESTCD"]);
                    }
                }
                if ($data["TB_NAMECD"] != "") {
                    if (strlen($data["TB_NAMECD"]) < 2 && is_numeric($data["TB_NAMECD"])) {
                        $data["TB_NAMECD"]           = sprintf("%02d", $data["TB_NAMECD"]);
                    }
                }
                if ($data["TB_ADVISECD"] != "") {
                    if (strlen($data["TB_ADVISECD"]) < 2 && is_numeric($data["TB_ADVISECD"])) {
                        $data["TB_ADVISECD"]         = sprintf("%02d", $data["TB_ADVISECD"]);
                    }
                }
                if ($data["HEART_MEDEXAM"] != "") {
                    if (strlen($data["HEART_MEDEXAM"]) < 2 && is_numeric($data["HEART_MEDEXAM"])) {
                        $data["HEART_MEDEXAM"]       = sprintf("%02d", $data["HEART_MEDEXAM"]);
                    }
                }
                if ($data["MANAGEMENT_DIV"] != "") {
                    if (strlen($data["MANAGEMENT_DIV"]) < 2 && is_numeric($data["MANAGEMENT_DIV"])) {
                        $data["MANAGEMENT_DIV"]      = sprintf("%02d", $data["MANAGEMENT_DIV"]);
                    }
                }
                if ($data["HEARTDISEASECD"] != "") {
                    if (strlen($data["HEARTDISEASECD"]) < 2 && is_numeric($data["HEARTDISEASECD"])) {
                        $data["HEARTDISEASECD"]      = sprintf("%02d", $data["HEARTDISEASECD"]);
                    }
                }
                if ($data["ALBUMINURIA1CD"] != "") {
                    if (strlen($data["ALBUMINURIA1CD"]) < 2 && is_numeric($data["ALBUMINURIA1CD"])) {
                        $data["ALBUMINURIA1CD"]      = sprintf("%02d", $data["ALBUMINURIA1CD"]);
                    }
                }
                if ($data["URICSUGAR1CD"] != "") {
                    if (strlen($data["URICSUGAR1CD"]) < 2 && is_numeric($data["URICSUGAR1CD"])) {
                        $data["URICSUGAR1CD"]        = sprintf("%02d", $data["URICSUGAR1CD"]);
                    }
                }
                if ($data["URICBLEED1CD"] != "") {
                    if (strlen($data["URICBLEED1CD"]) < 2 && is_numeric($data["URICBLEED1CD"])) {
                        $data["URICBLEED1CD"]        = sprintf("%02d", $data["URICBLEED1CD"]);
                    }
                }
                if ($data["ALBUMINURIA2CD"] != "") {
                    if (strlen($data["ALBUMINURIA2CD"]) < 2 && is_numeric($data["ALBUMINURIA2CD"])) {
                        $data["ALBUMINURIA2CD"]      = sprintf("%02d", $data["ALBUMINURIA2CD"]);
                    }
                }
                if ($data["URICSUGAR2CD"] != "") {
                    if (strlen($data["URICSUGAR2CD"]) < 2 && is_numeric($data["URICSUGAR2CD"])) {
                        $data["URICSUGAR2CD"]        = sprintf("%02d", $data["URICSUGAR2CD"]);
                    }
                }
                if ($data["URICBLEED2CD"] != "") {
                    if (strlen($data["URICBLEED2CD"]) < 2 && is_numeric($data["URICBLEED2CD"])) {
                        $data["URICBLEED2CD"]        = sprintf("%02d", $data["URICBLEED2CD"]);
                    }
                }
                if ($data["DETAILED_EXAMINATION"] != "") {
                    if (strlen($data["DETAILED_EXAMINATION"]) < 2 && is_numeric($data["DETAILED_EXAMINATION"])) {
                        $data["DETAILED_EXAMINATION"] = sprintf("%02d", $data["DETAILED_EXAMINATION"]);
                    }
                }
                if ($data["URI_ADVISECD"] != "") {
                    if (strlen($data["URI_ADVISECD"]) < 2 && is_numeric($data["URI_ADVISECD"])) {
                        $data["URI_ADVISECD"]        = sprintf("%02d", $data["URI_ADVISECD"]);
                    }
                }
                if ($data["TREATCD"] != "") {
                    if (strlen($data["TREATCD"]) < 2 && is_numeric($data["TREATCD"])) {
                        $data["TREATCD"]             = sprintf("%02d", $data["TREATCD"]);
                    }
                }
                if ($data["TREATCD2"] != "") {
                    if (strlen($data["TREATCD2"]) < 2 && is_numeric($data["TREATCD2"])) {
                        $data["TREATCD2"]            = sprintf("%02d", $data["TREATCD2"]);
                    }
                }
                if ($data["MEDICAL_HISTORY1"] != "") {
                    if (strlen($data["MEDICAL_HISTORY1"]) < 2 && is_numeric($data["MEDICAL_HISTORY1"])) {
                        $data["MEDICAL_HISTORY1"]    = sprintf("%02d", $data["MEDICAL_HISTORY1"]);
                    }
                }
                if ($data["MEDICAL_HISTORY2"] != "") {
                    if (strlen($data["MEDICAL_HISTORY2"]) < 2 && is_numeric($data["MEDICAL_HISTORY2"])) {
                        $data["MEDICAL_HISTORY2"]    = sprintf("%02d", $data["MEDICAL_HISTORY2"]);
                    }
                }
                if ($data["MEDICAL_HISTORY3"] != "") {
                    if (strlen($data["MEDICAL_HISTORY3"]) < 2 && is_numeric($data["MEDICAL_HISTORY3"])) {
                        $data["MEDICAL_HISTORY3"]    = sprintf("%02d", $data["MEDICAL_HISTORY3"]);
                    }
                }
                //歯・口腔-----
            } else {
                if ($data["JAWS_JOINTCD"] != "") {
                    if (strlen($data["JAWS_JOINTCD"]) < 2 && is_numeric($data["JAWS_JOINTCD"])) {
                        $data["JAWS_JOINTCD"] = sprintf("%02d", $data["JAWS_JOINTCD"]);
                    }
                }
                if ($data["JAWS_JOINTCD2"] != "") {
                    if (strlen($data["JAWS_JOINTCD2"]) < 2 && is_numeric($data["JAWS_JOINTCD2"])) {
                        $data["JAWS_JOINTCD2"] = sprintf("%02d", $data["JAWS_JOINTCD2"]);
                    }
                }
                if ($data["PLAQUECD"] != "") {
                    if (strlen($data["PLAQUECD"]) < 2 && is_numeric($data["PLAQUECD"])) {
                        $data["PLAQUECD"] = sprintf("%02d", $data["PLAQUECD"]);
                    }
                }
                if ($data["GUMCD"] != "") {
                    if (strlen($data["GUMCD"]) < 2 && is_numeric($data["GUMCD"])) {
                        $data["GUMCD"] = sprintf("%02d", $data["GUMCD"]);
                    }
                }
                if ($data["CALCULUS"] != "") {
                    if (strlen($data["CALCULUS"]) < 2 && is_numeric($data["CALCULUS"])) {
                        $data["CALCULUS"] = sprintf("%02d", $data["CALCULUS"]);
                    }
                }
                //乳歯 上
                if ($data["UP_R_BABY5"] != "") {
                    if (strlen($data["UP_R_BABY5"]) < 2 && is_numeric($data["UP_R_BABY5"])) {
                        $data["UP_R_BABY5"] = sprintf("%02d", $data["UP_R_BABY5"]);
                    }
                }
                if ($data["UP_R_BABY4"] != "") {
                    if (strlen($data["UP_R_BABY4"]) < 2 && is_numeric($data["UP_R_BABY4"])) {
                        $data["UP_R_BABY4"] = sprintf("%02d", $data["UP_R_BABY4"]);
                    }
                }
                if ($data["UP_R_BABY3"] != "") {
                    if (strlen($data["UP_R_BABY3"]) < 2 && is_numeric($data["UP_R_BABY3"])) {
                        $data["UP_R_BABY3"] = sprintf("%02d", $data["UP_R_BABY3"]);
                    }
                }
                if ($data["UP_R_BABY2"] != "") {
                    if (strlen($data["UP_R_BABY2"]) < 2 && is_numeric($data["UP_R_BABY2"])) {
                        $data["UP_R_BABY2"] = sprintf("%02d", $data["UP_R_BABY2"]);
                    }
                }
                if ($data["UP_R_BABY1"] != "") {
                    if (strlen($data["UP_R_BABY1"]) < 2 && is_numeric($data["UP_R_BABY1"])) {
                        $data["UP_R_BABY1"] = sprintf("%02d", $data["UP_R_BABY1"]);
                    }
                }
                if ($data["UP_L_BABY1"] != "") {
                    if (strlen($data["UP_L_BABY1"]) < 2 && is_numeric($data["UP_L_BABY1"])) {
                        $data["UP_L_BABY1"] = sprintf("%02d", $data["UP_L_BABY1"]);
                    }
                }
                if ($data["UP_L_BABY2"] != "") {
                    if (strlen($data["UP_L_BABY2"]) < 2 && is_numeric($data["UP_L_BABY2"])) {
                        $data["UP_L_BABY2"] = sprintf("%02d", $data["UP_L_BABY2"]);
                    }
                }
                if ($data["UP_L_BABY3"] != "") {
                    if (strlen($data["UP_L_BABY3"]) < 2 && is_numeric($data["UP_L_BABY3"])) {
                        $data["UP_L_BABY3"] = sprintf("%02d", $data["UP_L_BABY3"]);
                    }
                }
                if ($data["UP_L_BABY4"] != "") {
                    if (strlen($data["UP_L_BABY4"]) < 2 && is_numeric($data["UP_L_BABY4"])) {
                        $data["UP_L_BABY4"] = sprintf("%02d", $data["UP_L_BABY4"]);
                    }
                }
                if ($data["UP_L_BABY5"] != "") {
                    if (strlen($data["UP_L_BABY5"]) < 2 && is_numeric($data["UP_L_BABY5"])) {
                        $data["UP_L_BABY5"] = sprintf("%02d", $data["UP_L_BABY5"]);
                    }
                }
                //乳歯 下
                if ($data["LW_R_BABY5"] != "") {
                    if (strlen($data["LW_R_BABY5"]) < 2 && is_numeric($data["LW_R_BABY5"])) {
                        $data["LW_R_BABY5"] = sprintf("%02d", $data["LW_R_BABY5"]);
                    }
                }
                if ($data["LW_R_BABY4"] != "") {
                    if (strlen($data["LW_R_BABY4"]) < 2 && is_numeric($data["LW_R_BABY4"])) {
                        $data["LW_R_BABY4"] = sprintf("%02d", $data["LW_R_BABY4"]);
                    }
                }
                if ($data["LW_R_BABY3"] != "") {
                    if (strlen($data["LW_R_BABY3"]) < 2 && is_numeric($data["LW_R_BABY3"])) {
                        $data["LW_R_BABY3"] = sprintf("%02d", $data["LW_R_BABY3"]);
                    }
                }
                if ($data["LW_R_BABY2"] != "") {
                    if (strlen($data["LW_R_BABY2"]) < 2 && is_numeric($data["LW_R_BABY2"])) {
                        $data["LW_R_BABY2"] = sprintf("%02d", $data["LW_R_BABY2"]);
                    }
                }
                if ($data["LW_R_BABY1"] != "") {
                    if (strlen($data["LW_R_BABY1"]) < 2 && is_numeric($data["LW_R_BABY1"])) {
                        $data["LW_R_BABY1"] = sprintf("%02d", $data["LW_R_BABY1"]);
                    }
                }
                if ($data["LW_L_BABY1"] != "") {
                    if (strlen($data["LW_L_BABY1"]) < 2 && is_numeric($data["LW_L_BABY1"])) {
                        $data["LW_L_BABY1"] = sprintf("%02d", $data["LW_L_BABY1"]);
                    }
                }
                if ($data["LW_L_BABY2"] != "") {
                    if (strlen($data["LW_L_BABY2"]) < 2 && is_numeric($data["LW_L_BABY2"])) {
                        $data["LW_L_BABY2"] = sprintf("%02d", $data["LW_L_BABY2"]);
                    }
                }
                if ($data["LW_L_BABY3"] != "") {
                    if (strlen($data["LW_L_BABY3"]) < 2 && is_numeric($data["LW_L_BABY3"])) {
                        $data["LW_L_BABY3"] = sprintf("%02d", $data["LW_L_BABY3"]);
                    }
                }
                if ($data["LW_L_BABY4"] != "") {
                    if (strlen($data["LW_L_BABY4"]) < 2 && is_numeric($data["LW_L_BABY4"])) {
                        $data["LW_L_BABY4"] = sprintf("%02d", $data["LW_L_BABY4"]);
                    }
                }
                if ($data["LW_L_BABY5"] != "") {
                    if (strlen($data["LW_L_BABY5"]) < 2 && is_numeric($data["LW_L_BABY5"])) {
                        $data["LW_L_BABY5"] = sprintf("%02d", $data["LW_L_BABY5"]);
                    }
                }
                //永久歯 上
                if ($data["UP_R_ADULT8"] != "") {
                    if (strlen($data["UP_R_ADULT8"]) < 2 && is_numeric($data["UP_R_ADULT8"])) {
                        $data["UP_R_ADULT8"] = sprintf("%02d", $data["UP_R_ADULT8"]);
                    }
                }
                if ($data["UP_R_ADULT7"] != "") {
                    if (strlen($data["UP_R_ADULT7"]) < 2 && is_numeric($data["UP_R_ADULT7"])) {
                        $data["UP_R_ADULT7"] = sprintf("%02d", $data["UP_R_ADULT7"]);
                    }
                }
                if ($data["UP_R_ADULT6"] != "") {
                    if (strlen($data["UP_R_ADULT6"]) < 2 && is_numeric($data["UP_R_ADULT6"])) {
                        $data["UP_R_ADULT6"] = sprintf("%02d", $data["UP_R_ADULT6"]);
                    }
                }
                if ($data["UP_R_ADULT5"] != "") {
                    if (strlen($data["UP_R_ADULT5"]) < 2 && is_numeric($data["UP_R_ADULT5"])) {
                        $data["UP_R_ADULT5"] = sprintf("%02d", $data["UP_R_ADULT5"]);
                    }
                }
                if ($data["UP_R_ADULT4"] != "") {
                    if (strlen($data["UP_R_ADULT4"]) < 2 && is_numeric($data["UP_R_ADULT4"])) {
                        $data["UP_R_ADULT4"] = sprintf("%02d", $data["UP_R_ADULT4"]);
                    }
                }
                if ($data["UP_R_ADULT3"] != "") {
                    if (strlen($data["UP_R_ADULT3"]) < 2 && is_numeric($data["UP_R_ADULT3"])) {
                        $data["UP_R_ADULT3"] = sprintf("%02d", $data["UP_R_ADULT3"]);
                    }
                }
                if ($data["UP_R_ADULT2"] != "") {
                    if (strlen($data["UP_R_ADULT2"]) < 2 && is_numeric($data["UP_R_ADULT2"])) {
                        $data["UP_R_ADULT2"] = sprintf("%02d", $data["UP_R_ADULT2"]);
                    }
                }
                if ($data["UP_R_ADULT1"] != "") {
                    if (strlen($data["UP_R_ADULT1"]) < 2 && is_numeric($data["UP_R_ADULT1"])) {
                        $data["UP_R_ADULT1"] = sprintf("%02d", $data["UP_R_ADULT1"]);
                    }
                }
                if ($data["UP_L_ADULT1"] != "") {
                    if (strlen($data["UP_L_ADULT1"]) < 2 && is_numeric($data["UP_L_ADULT1"])) {
                        $data["UP_L_ADULT1"] = sprintf("%02d", $data["UP_L_ADULT1"]);
                    }
                }
                if ($data["UP_L_ADULT2"] != "") {
                    if (strlen($data["UP_L_ADULT2"]) < 2 && is_numeric($data["UP_L_ADULT2"])) {
                        $data["UP_L_ADULT2"] = sprintf("%02d", $data["UP_L_ADULT2"]);
                    }
                }
                if ($data["UP_L_ADULT3"] != "") {
                    if (strlen($data["UP_L_ADULT3"]) < 2 && is_numeric($data["UP_L_ADULT3"])) {
                        $data["UP_L_ADULT3"] = sprintf("%02d", $data["UP_L_ADULT3"]);
                    }
                }
                if ($data["UP_L_ADULT4"] != "") {
                    if (strlen($data["UP_L_ADULT4"]) < 2 && is_numeric($data["UP_L_ADULT4"])) {
                        $data["UP_L_ADULT4"] = sprintf("%02d", $data["UP_L_ADULT4"]);
                    }
                }
                if ($data["UP_L_ADULT5"] != "") {
                    if (strlen($data["UP_L_ADULT5"]) < 2 && is_numeric($data["UP_L_ADULT5"])) {
                        $data["UP_L_ADULT5"] = sprintf("%02d", $data["UP_L_ADULT5"]);
                    }
                }
                if ($data["UP_L_ADULT6"] != "") {
                    if (strlen($data["UP_L_ADULT6"]) < 2 && is_numeric($data["UP_L_ADULT6"])) {
                        $data["UP_L_ADULT6"] = sprintf("%02d", $data["UP_L_ADULT6"]);
                    }
                }
                if ($data["UP_L_ADULT7"] != "") {
                    if (strlen($data["UP_L_ADULT7"]) < 2 && is_numeric($data["UP_L_ADULT7"])) {
                        $data["UP_L_ADULT7"] = sprintf("%02d", $data["UP_L_ADULT7"]);
                    }
                }
                if ($data["UP_L_ADULT8"] != "") {
                    if (strlen($data["UP_L_ADULT8"]) < 2 && is_numeric($data["UP_L_ADULT8"])) {
                        $data["UP_L_ADULT8"] = sprintf("%02d", $data["UP_L_ADULT8"]);
                    }
                }
                //永久歯 下
                if ($data["LW_R_ADULT8"] != "") {
                    if (strlen($data["LW_R_ADULT8"]) < 2 && is_numeric($data["LW_R_ADULT8"])) {
                        $data["LW_R_ADULT8"] = sprintf("%02d", $data["LW_R_ADULT8"]);
                    }
                }
                if ($data["LW_R_ADULT7"] != "") {
                    if (strlen($data["LW_R_ADULT7"]) < 2 && is_numeric($data["LW_R_ADULT7"])) {
                        $data["LW_R_ADULT7"] = sprintf("%02d", $data["LW_R_ADULT7"]);
                    }
                }
                if ($data["LW_R_ADULT6"] != "") {
                    if (strlen($data["LW_R_ADULT6"]) < 2 && is_numeric($data["LW_R_ADULT6"])) {
                        $data["LW_R_ADULT6"] = sprintf("%02d", $data["LW_R_ADULT6"]);
                    }
                }
                if ($data["LW_R_ADULT5"] != "") {
                    if (strlen($data["LW_R_ADULT5"]) < 2 && is_numeric($data["LW_R_ADULT5"])) {
                        $data["LW_R_ADULT5"] = sprintf("%02d", $data["LW_R_ADULT5"]);
                    }
                }
                if ($data["LW_R_ADULT4"] != "") {
                    if (strlen($data["LW_R_ADULT4"]) < 2 && is_numeric($data["LW_R_ADULT4"])) {
                        $data["LW_R_ADULT4"] = sprintf("%02d", $data["LW_R_ADULT4"]);
                    }
                }
                if ($data["LW_R_ADULT3"] != "") {
                    if (strlen($data["LW_R_ADULT3"]) < 2 && is_numeric($data["LW_R_ADULT3"])) {
                        $data["LW_R_ADULT3"] = sprintf("%02d", $data["LW_R_ADULT3"]);
                    }
                }
                if ($data["LW_R_ADULT2"] != "") {
                    if (strlen($data["LW_R_ADULT2"]) < 2 && is_numeric($data["LW_R_ADULT2"])) {
                        $data["LW_R_ADULT2"] = sprintf("%02d", $data["LW_R_ADULT2"]);
                    }
                }
                if ($data["LW_R_ADULT1"] != "") {
                    if (strlen($data["LW_R_ADULT1"]) < 2 && is_numeric($data["LW_R_ADULT1"])) {
                        $data["LW_R_ADULT1"] = sprintf("%02d", $data["LW_R_ADULT1"]);
                    }
                }
                if ($data["LW_L_ADULT1"] != "") {
                    if (strlen($data["LW_L_ADULT1"]) < 2 && is_numeric($data["LW_L_ADULT1"])) {
                        $data["LW_L_ADULT1"] = sprintf("%02d", $data["LW_L_ADULT1"]);
                    }
                }
                if ($data["LW_L_ADULT2"] != "") {
                    if (strlen($data["LW_L_ADULT2"]) < 2 && is_numeric($data["LW_L_ADULT2"])) {
                        $data["LW_L_ADULT2"] = sprintf("%02d", $data["LW_L_ADULT2"]);
                    }
                }
                if ($data["LW_L_ADULT3"] != "") {
                    if (strlen($data["LW_L_ADULT3"]) < 2 && is_numeric($data["LW_L_ADULT3"])) {
                        $data["LW_L_ADULT3"] = sprintf("%02d", $data["LW_L_ADULT3"]);
                    }
                }
                if ($data["LW_L_ADULT4"] != "") {
                    if (strlen($data["LW_L_ADULT4"]) < 2 && is_numeric($data["LW_L_ADULT4"])) {
                        $data["LW_L_ADULT4"] = sprintf("%02d", $data["LW_L_ADULT4"]);
                    }
                }
                if ($data["LW_L_ADULT5"] != "") {
                    if (strlen($data["LW_L_ADULT5"]) < 2 && is_numeric($data["LW_L_ADULT5"])) {
                        $data["LW_L_ADULT5"] = sprintf("%02d", $data["LW_L_ADULT5"]);
                    }
                }
                if ($data["LW_L_ADULT6"] != "") {
                    if (strlen($data["LW_L_ADULT6"]) < 2 && is_numeric($data["LW_L_ADULT6"])) {
                        $data["LW_L_ADULT6"] = sprintf("%02d", $data["LW_L_ADULT6"]);
                    }
                }
                if ($data["LW_L_ADULT7"] != "") {
                    if (strlen($data["LW_L_ADULT7"]) < 2 && is_numeric($data["LW_L_ADULT7"])) {
                        $data["LW_L_ADULT7"] = sprintf("%02d", $data["LW_L_ADULT7"]);
                    }
                }
                if ($data["LW_L_ADULT8"] != "") {
                    if (strlen($data["LW_L_ADULT8"]) < 2 && is_numeric($data["LW_L_ADULT8"])) {
                        $data["LW_L_ADULT8"] = sprintf("%02d", $data["LW_L_ADULT8"]);
                    }
                }
                //その他疾病及異常コード
                if ($data["OTHERDISEASECD"] != "") {
                    if (strlen($data["OTHERDISEASECD"]) < 2 && is_numeric($data["OTHERDISEASECD"])) {
                        $data["OTHERDISEASECD"] = sprintf("%02d", $data["OTHERDISEASECD"]);
                    }
                }
                if ($data["OTHERDISEASECD2"]!= "") {
                    if (strlen($data["OTHERDISEASECD2"]) < 2 && is_numeric($data["OTHERDISEASECD2"])) {
                        $data["OTHERDISEASECD2"] = sprintf("%02d", $data["OTHERDISEASECD2"]);
                    }
                }
                //学校歯科医所見コード
                if ($data["DENTISTREMARKCD"] != "") {
                    if (strlen($data["DENTISTREMARKCD"]) < 2 && is_numeric($data["DENTISTREMARKCD"])) {
                        $data["DENTISTREMARKCD"] = sprintf("%02d", $data["DENTISTREMARKCD"]);
                    }
                }
                //学校歯科医所見コード2
                if ($data["DENTISTREMARKCD2"] != "") {
                    if (strlen($data["DENTISTREMARKCD2"]) < 2 && is_numeric($data["DENTISTREMARKCD2"])) {
                        $data["DENTISTREMARKCD2"] = sprintf("%02d", $data["DENTISTREMARKCD2"]);
                    }
                }
                //学校歯科医所見コード3
                if ($data["DENTISTREMARKCD3"] != "") {
                    if (strlen($data["DENTISTREMARKCD3"]) < 2 && is_numeric($data["DENTISTREMARKCD3"])) {
                        $data["DENTISTREMARKCD3"] = sprintf("%02d", $data["DENTISTREMARKCD3"]);
                    }
                }
                //学校歯科医事後処置コード1
                if ($data["DENTISTTREATCD"] != "") {
                    if (strlen($data["DENTISTTREATCD"]) < 2 && is_numeric($data["DENTISTTREATCD"])) {
                        $data["DENTISTTREATCD"] = sprintf("%02d", $data["DENTISTTREATCD"]);
                    }
                }
                //学校歯科医事後処置コード2
                if ($data["DENTISTTREATCD2"] != "") {
                    if (strlen($data["DENTISTTREATCD2"]) < 2 && is_numeric($data["DENTISTTREATCD2"])) {
                        $data["DENTISTTREATCD2"] = sprintf("%02d", $data["DENTISTTREATCD2"]);
                    }
                }
            }//csvcd
        //削除
        } else {
            if ($data["SCHREGNO"] != "") {
                if (strlen($data["SCHREGNO"]) < 8 && is_numeric($data["SCHREGNO"])) {
                    $data["SCHREGNO"] = sprintf("%08d", $data["SCHREGNO"]);
                }
            }
        }
        return $data;
    }

    //所見欄を無効にするcdの文字列
    public function getDisableCodes($db, $setInNamecd1, $year)
    {
        $query = knjf140jQuery::getNameMstDisableCodes($this, $setInNamecd1, $year);
        $disableCodes = array();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $disableCodes[] = $row["NAMECD2"];
        }
        $result->free();

        return $disableCodes;
    }

    //データの整合性チェック
    public function isDataMatched(&$dataArr)
    {
        //１万件を超えたら制限エラー
        if (get_count($dataArr) > 10000) {
            $this->setWarning("MSG915", "処理件数を１万件以下にして下さい 処理件数：".get_count($dataArr)."件");
            return false;
        }
        return true;
    }
}
