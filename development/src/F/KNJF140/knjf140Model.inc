<?php

require_once('for_php7.php');
class knjf140Model extends Model
{
    public $field    = array();
    public $data     = array();
    public $cmd;
    public $control; //コントロールマスタ
    public $staffcd; //登録者コード
    public $schoolCd;
    public $getPrgId;  //コールされたか
    public $sendAuth;  //親画面の権限
    public $auth;      //権限
    public $lastColumn;//CSVの最後の列の値

    public function knjf140Model()
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

        $this->field = array("OUTPUT"         =>  VARS::post("OUTPUT"),         //出力取込種別
                             "YEAR"           =>  VARS::post("YEAR"),           //年度一覧
                             "GRADE_HR_CLASS" =>  VARS::post("GRADE_HR_CLASS"), //年組一覧
                             "HEADER"         =>  VARS::post("HEADER"),         //ヘッダ有
                             "CSVCD"          =>  VARS::post("CSVCD"),          //対象データ
                             "SHORI_MEI"      =>  VARS::post("SHORI_MEI")       //処理名
                             );

        $this->cmd = VARS::request("cmd");
        if (VARS::file("FILE")) {
            $this->field["FILE"] = VARS::file("FILE");        //ファイルのアップロード
            $this->field["FILE"]["name"] = i18n_convert($this->field["FILE"]["name"], "UTF-8", "SJIS-win");
        }

        $this->getPropertiesAll(); //プロパティファイル
        $this->lastColumn = 'DUMMY';
        if ($this->Properties["csv_LastColumn"] != '') {
            $this->lastColumn = $this->Properties["csv_LastColumn"];
        }
    }

    public function &getMainModel()
    {
    }

    //ファイル取り込み
    public function &getExecModel()
    {
        $this->dataArr = array();
        $this->dataArr = $this->getFileString();
        if ($this->isDataMatched($this->dataArr)) {
            $errFlg = $this->checkCsvErrQuery(PROGRAMID);
            if ($errFlg) {
                $db = Query::dbCheckOut();
                $db->autoCommit(false);
                for ($i = 0; $i < get_count($this->dataArr); $i++) {
                    //更新
                    if ($this->field["SHORI_MEI"] == "1") {
                        //delete and insert
                        knjf140Query::deleteinsertQueryCsv($db, $this->dataArr[$i], $this->field["CSVCD"]);
                    //削除
                    } else {
                        //delete
                        knjf140Query::deleteQueryCsv($db, $this->dataArr[$i], $this->field["CSVCD"]);
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
        $tempArr = array();
        if (is_uploaded_file($this->field["FILE"]["tmp_name"])) { //HTTP POSTによりアップロードされたファイルかどうかを調べる
            $checkdata = array();
            if ($this->field["CSVCD"] == "1") { //一般
                $checkdata = array();
                $checkdata["YEAR"]                  = array(4, "年度");
                $checkdata["DATE"]                  = array(10, "健診実施日");
                $checkdata["SCHREGNO"]              = array(8, "学籍番号");
                $checkdata["HEIGHT"]                = array(5, "身長");
                $checkdata["WEIGHT"]                = array(5, "体重");
                $checkdata["SITHEIGHT"]             = array(5, "座高");
                $checkdata["CHEST"]                 = array(5, "胸囲");
                $checkdata["R_BAREVISION"]          = array(4, "視力右裸眼(数字)");
                $checkdata["R_BAREVISION_MARK"]     = array(3, "視力右裸眼(文字)");
                $checkdata["L_BAREVISION"]          = array(4, "視力左裸眼(数字)");
                $checkdata["L_BAREVISION_MARK"]     = array(3, "視力左裸眼(文字)");
                $checkdata["R_VISION"]              = array(4, "視力右矯正(数字)");
                $checkdata["R_VISION_MARK"]         = array(3, "視力右矯正(文字)");
                $checkdata["L_VISION"]              = array(4, "視力左矯正(数字)");
                $checkdata["L_VISION_MARK"]         = array(3, "視力左矯正(文字)");
                $checkdata["R_EAR"]                 = array(2, "聴力右状態コード", "F010");
                $checkdata["R_EAR_DB"]              = array(3, "聴力右DB");
                $checkdata["L_EAR"]                 = array(2, "聴力左状態コード", "F010");
                $checkdata["L_EAR_DB"]              = array(3, "聴力左DB");
                $checkdata["ALBUMINURIA1CD"]        = array(2, "一次尿蛋白コード", "F020");
                $checkdata["URICSUGAR1CD"]          = array(2, "一次尿糖コード", "F019");
                $checkdata["URICBLEED1CD"]          = array(2, "一次尿潜血コード", "F018");
                $checkdata["ALBUMINURIA2CD"]        = array(2, "二次尿蛋白コード", "F020");
                $checkdata["URICSUGAR2CD"]          = array(2, "二次尿糖コード", "F019");
                $checkdata["URICBLEED2CD"]          = array(2, "二次尿潜血コード", "F018");
                $checkdata["URICOTHERTEST"]         = array(60, "尿その他の検査");
                $checkdata["URI_ADVISECD"]          = array(2, "尿その他の検査 指導区分", "F021");
                $checkdata["NUTRITIONCD"]           = array(2, "栄養状態コード", "F030");
                $checkdata["SPINERIBCD"]            = array(2, "脊柱胸郭コード", "F040");
                $checkdata["SPINERIBCD_REMARK"]     = array(60, "脊柱胸郭所見");
                $checkdata["EYEDISEASECD"]          = array(2, "目疾病及異常コード", "F050");
                $checkdata["EYE_TEST_RESULT"]       = array(60, "眼科所見");
                $checkdata["NOSEDISEASECD"]         = array(2, "耳鼻咽頭疾患コード", "F060");
                $checkdata["NOSEDISEASECD_REMARK"]  = array(60, "耳鼻咽頭疾患所見");
                $checkdata["SKINDISEASECD"]         = array(2, "皮膚疾患コード", "F070");
                $checkdata["HEART_MEDEXAM"]         = array(2, "心臓臨床医学的検査コード", "F080");
                $checkdata["HEART_MEDEXAM_REMARK"]  = array(120, "心臓臨床医学的検査");
                $checkdata["HEARTDISEASECD"]        = array(2, "心臓疾病及異常コード", "F090");
                $checkdata["HEARTDISEASECD_REMARK"] = array(60, "心臓疾病及異常所見");
                $checkdata["MANAGEMENT_DIV"]        = array(2, "心臓管理区分", "F091");
                $checkdata["TB_DATE"]               = array(10, "結核ツ反実施日");
                $checkdata["TB_REACT"]              = array(4, "結核ツ反応");
                $checkdata["TB_RESULT"]             = array(2, "結核ツ反判定コード", "F020");
                $checkdata["TB_BCGDATE"]            = array(10, "結核BCG検査日");
                $checkdata["TB_FILMDATE"]           = array(10, "結核撮影日付");
                $checkdata["TB_FILMNO"]             = array(6, "結核フィルム番号");
                $checkdata["TB_REMARKCD"]           = array(2, "結核所見コード", "F100");
                $checkdata["TB_OTHERTESTCD"]        = array(2, "結核その他検査コード", "F110");
                $checkdata["TB_NAMECD"]             = array(2, "結核病名コード", "F120");
                $checkdata["TB_ADVISECD"]           = array(2, "結核指導区分コード", "F130");
                $checkdata["TB_X_RAY"]              = array(60, "結核所見(X線)");
                $checkdata["ANEMIA_REMARK"]         = array(30, "貧血所見");
                $checkdata["HEMOGLOBIN"]            = array(4, "貧血ヘモグロビン値");
                $checkdata["PARASITE"]              = array(2, "寄生虫卵");
                $checkdata["OTHERDISEASECD"]        = array(2, "その他疾病及異常コード", "F140");
                $checkdata["OTHER_ADVISECD"]        = array(2, "その他疾病及異常 指導区分", "F145");
                $checkdata["DOC_CD"]                = array(2, "学校医コード(内科検診)", "F144");
                $checkdata["DOC_REMARK"]            = array(30, "学校医所見");
                $checkdata["DOC_DATE"]              = array(10, "学校医日付");
                $checkdata["TREATCD"]               = array(2, "事後処置コード", "F150");
                $checkdata["REMARK"]                = array(30, "備考");
                $checkdata["NUTRITION_RESULT"]      = array(60, "内科検診結果(栄養状態)");
                $checkdata["EYEDISEASE_RESULT"]     = array(60, "眼科検診結果(目疾病及異常)");
                $checkdata["SKINDISEASE_RESULT"]    = array(60, "皮膚科検診結果(皮膚疾患)");
                $checkdata["SPINERIB_RESULT"]       = array(60, "脊柱胸郭検診結果(脊柱胸郭)");
                $checkdata["NOSEDISEASE_RESULT"]    = array(60, "耳鼻科検診結果(耳鼻咽頭疾患)");
                $checkdata["OTHERDISEASE_RESULT"]   = array(60, "その他検診結果(その他疾病及異常)");
                $checkdata["HEARTDISEASE_RESULT"]   = array(60, "心臓検診結果(心臓疾病及異常)");
                $checkdata["GUIDE_DIV"]             = array(1, "指導区分", "F141");
                $checkdata["JOINING_SPORTS_CLUB"]   = array(1, "運動部活動", "F142");
                $checkdata["MEDICAL_HISTORY1"]      = array(3, "既往症1", "F143");
                $checkdata["MEDICAL_HISTORY2"]      = array(3, "既往症2", "F143");
                $checkdata["MEDICAL_HISTORY3"]      = array(3, "既往症3", "F143");
                $checkdata["DIAGNOSIS_NAME"]        = array(150, "診断名");
            } else { //歯・口腔
                $checkdata = array();
                $checkdata["YEAR"]              = array(4, "年度");
                $checkdata["DATE"]              = array(10, "健診実施日");
                $checkdata["SCHREGNO"]          = array(8, "学籍番号");
                $checkdata["JAWS_JOINTCD"]      = array(2, "歯列咬合顎関節コード", "F510");
                $checkdata["JAWS_JOINTCD2"]     = array(2, "顎関節コード", "F511");
                $checkdata["PLAQUECD"]          = array(2, "歯垢状態コード", "F520");
                $checkdata["GUMCD"]             = array(2, "歯肉状態コード", "F510");
                $checkdata["ORTHODONTICS"]      = array(1, "矯正の有無");
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
                $checkdata["DENTISTREMARKCD"]   = array(2, "学校歯科医所見コード", "F540");
                $checkdata["DENTISTREMARK"]     = array(30, "学校医処理");
                $checkdata["DENTISTREMARKDATE"] = array(10, "学校歯科医所見日付");
                $checkdata["DENTISTTREAT"]      = array(30, "学校歯科医事後処置");
            }
            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            //エラーデータの削除
            $query = $this->deleteQueryErr(PROGRAMID);
            $result = $db->query($query);
            $i = 0;
            $record_no = 1; //行番号(初期値)
            $primaryKey1 = array();
            $primaryKey2 = array();
            $duplicateCsv1 = array();
            $duplicateCsv2 = array();
            if ($this->field["CSVCD"] == "1") {
                $primaryKey1 = $this->getPrimaryKey('MEDEXAM_DET_DAT');
            } else {
                $primaryKey1 = $this->getPrimaryKey('MEDEXAM_TOOTH_DAT');
            }
            $primaryKey2 = $this->getPrimaryKey('MEDEXAM_HDAT');

            $fp = fopen($this->field["FILE"]["tmp_name"], "r"); //ファイルまたはURLをオープンする
            while (!feof($fp)) {                                //ファイルポインタがファイル終端に達しているかどうか調べる
                $buffer = fgets($fp, 4096);                      //ファイルポインタから1行取得する
                if (trim($buffer) != '') {
                    //ヘッダ有なら１行目は無視
                    if ($this->field["HEADER"] == "on" && $record_no < 2) {
                        $record_no++;
                        continue;
                    }
                    $buffer = i18n_convert($buffer, "UTF-8", "SJIS-win");        //文字列を指定コードに変換する

                    $tmp = explode(",", $buffer);

                    if ($this->field["CSVCD"] == "1") { //一般
                        $data = array("CSVCD"                => trim($tmp[0]),
                                      "YEAR"                 => trim($tmp[1]),
                                      "DATE"                 => str_replace("/", "-", trim($tmp[2])),
                                      "SCHREGNO"             => trim($tmp[3]),
                                      "HEIGHT"               => trim($tmp[4]),
                                      "WEIGHT"               => trim($tmp[5]),
                                      "SITHEIGHT"            => trim($tmp[6]),
                                      "CHEST"                => trim($tmp[7]),
                                      "R_BAREVISION"         => trim($tmp[8]),
                                      "R_BAREVISION_MARK"    => trim($tmp[9]),
                                      "L_BAREVISION"         => trim($tmp[10]),
                                      "L_BAREVISION_MARK"    => trim($tmp[11]),
                                      "R_VISION"             => trim($tmp[12]),
                                      "R_VISION_MARK"        => trim($tmp[13]),
                                      "L_VISION"             => trim($tmp[14]),
                                      "L_VISION_MARK"        => trim($tmp[15]),
                                      "R_EAR"                => trim($tmp[16]),
                                      "R_EAR_DB"             => trim($tmp[17]),
                                      "L_EAR"                => trim($tmp[18]),
                                      "L_EAR_DB"             => trim($tmp[19]),
                                      "ALBUMINURIA1CD"       => trim($tmp[20]),
                                      "URICSUGAR1CD"         => trim($tmp[21]),
                                      "URICBLEED1CD"         => trim($tmp[22]),
                                      "ALBUMINURIA2CD"       => trim($tmp[23]),
                                      "URICSUGAR2CD"         => trim($tmp[24]),
                                      "URICBLEED2CD"         => trim($tmp[25]),
                                      "URICOTHERTEST"        => trim($tmp[26]),
                                      "URI_ADVISECD"         => trim($tmp[27]),
                                      "NUTRITIONCD"          => trim($tmp[28]),
                                      "SPINERIBCD"           => trim($tmp[29]),
                                      "SPINERIBCD_REMARK"    => trim($tmp[30]),
                                      "EYEDISEASECD"         => trim($tmp[31]),
                                      "EYE_TEST_RESULT"      => trim($tmp[32]),
                                      "NOSEDISEASECD"        => trim($tmp[33]),
                                      "NOSEDISEASECD_REMARK" => trim($tmp[34]),
                                      "SKINDISEASECD"        => trim($tmp[35]),
                                      "HEART_MEDEXAM"        => trim($tmp[36]),
                                      "HEART_MEDEXAM_REMARK" => trim($tmp[37]),
                                      "HEARTDISEASECD"       => trim($tmp[38]),
                                      "HEARTDISEASECD_REMARK"=> trim($tmp[39]),
                                      "MANAGEMENT_DIV"       => trim($tmp[40]),
                                      "TB_DATE"              => str_replace("/", "-", trim($tmp[41])),
                                      "TB_REACT"             => trim($tmp[42]),
                                      "TB_RESULT"            => trim($tmp[43]),
                                      "TB_BCGDATE"           => str_replace("/", "-", trim($tmp[44])),
                                      "TB_FILMDATE"          => str_replace("/", "-", trim($tmp[45])),
                                      "TB_FILMNO"            => trim($tmp[46]),
                                      "TB_REMARKCD"          => trim($tmp[47]),
                                      "TB_OTHERTESTCD"       => trim($tmp[48]),
                                      "TB_NAMECD"            => trim($tmp[49]),
                                      "TB_ADVISECD"          => trim($tmp[50]),
                                      "TB_X_RAY"             => trim($tmp[51]),
                                      "ANEMIA_REMARK"        => trim($tmp[52]),
                                      "HEMOGLOBIN"           => trim($tmp[53]),
                                      "PARASITE"             => trim($tmp[54]),
                                      "OTHERDISEASECD"       => trim($tmp[55]),
                                      "OTHER_ADVISECD"       => trim($tmp[56]),
                                      "DOC_CD"               => trim($tmp[57]),
                                      "DOC_REMARK"           => trim($tmp[58]),
                                      "DOC_DATE"             => str_replace("/", "-", trim($tmp[59])),
                                      "TREATCD"              => trim($tmp[60]),
                                      "REMARK"               => trim($tmp[61]),
                                      "NUTRITION_RESULT"     => trim($tmp[62]),
                                      "EYEDISEASE_RESULT"    => trim($tmp[63]),
                                      "SKINDISEASE_RESULT"   => trim($tmp[64]),
                                      "SPINERIB_RESULT"      => trim($tmp[65]),
                                      "NOSEDISEASE_RESULT"   => trim($tmp[66]),
                                      "OTHERDISEASE_RESULT"  => trim($tmp[67]),
                                      "HEARTDISEASE_RESULT"  => trim($tmp[68]),
                                      "GUIDE_DIV"            => trim($tmp[69]),
                                      "JOINING_SPORTS_CLUB"  => trim($tmp[70]),
                                      "MEDICAL_HISTORY1"     => trim($tmp[71]),
                                      "MEDICAL_HISTORY2"     => trim($tmp[72]),
                                      "MEDICAL_HISTORY3"     => trim($tmp[73]),
                                      "DIAGNOSIS_NAME"       => trim($tmp[74])
                                      );
                    } else { //歯・口腔
                        $data = array("CSVCD"             => trim($tmp[0]),
                                      "YEAR"              => trim($tmp[1]),
                                      "DATE"              => str_replace("/", "-", trim($tmp[2])),
                                      "SCHREGNO"          => trim($tmp[3]),
                                      "JAWS_JOINTCD"      => trim($tmp[4]),
                                      "JAWS_JOINTCD2"     => trim($tmp[5]),
                                      "PLAQUECD"          => trim($tmp[6]),
                                      "GUMCD"             => trim($tmp[7]),
                                      "ORTHODONTICS"      => trim($tmp[8]),
                                      "UP_R_BABY5"        => trim($tmp[9]),
                                      "UP_R_BABY4"        => trim($tmp[10]),
                                      "UP_R_BABY3"        => trim($tmp[11]),
                                      "UP_R_BABY2"        => trim($tmp[12]),
                                      "UP_R_BABY1"        => trim($tmp[13]),
                                      "UP_L_BABY1"        => trim($tmp[14]),
                                      "UP_L_BABY2"        => trim($tmp[15]),
                                      "UP_L_BABY3"        => trim($tmp[16]),
                                      "UP_L_BABY4"        => trim($tmp[17]),
                                      "UP_L_BABY5"        => trim($tmp[18]),
                                      "LW_R_BABY5"        => trim($tmp[19]),
                                      "LW_R_BABY4"        => trim($tmp[20]),
                                      "LW_R_BABY3"        => trim($tmp[21]),
                                      "LW_R_BABY2"        => trim($tmp[22]),
                                      "LW_R_BABY1"        => trim($tmp[23]),
                                      "LW_L_BABY1"        => trim($tmp[24]),
                                      "LW_L_BABY2"        => trim($tmp[25]),
                                      "LW_L_BABY3"        => trim($tmp[26]),
                                      "LW_L_BABY4"        => trim($tmp[27]),
                                      "LW_L_BABY5"        => trim($tmp[28]),
                                      "BABYTOOTH"         => trim($tmp[29]),
                                      "REMAINBABYTOOTH"   => trim($tmp[30]),
                                      "TREATEDBABYTOOTH"  => trim($tmp[31]),
                                      "BRACK_BABYTOOTH"   => trim($tmp[32]),
                                      "UP_R_ADULT8"       => trim($tmp[33]),
                                      "UP_R_ADULT7"       => trim($tmp[34]),
                                      "UP_R_ADULT6"       => trim($tmp[35]),
                                      "UP_R_ADULT5"       => trim($tmp[36]),
                                      "UP_R_ADULT4"       => trim($tmp[37]),
                                      "UP_R_ADULT3"       => trim($tmp[38]),
                                      "UP_R_ADULT2"       => trim($tmp[39]),
                                      "UP_R_ADULT1"       => trim($tmp[40]),
                                      "UP_L_ADULT1"       => trim($tmp[41]),
                                      "UP_L_ADULT2"       => trim($tmp[42]),
                                      "UP_L_ADULT3"       => trim($tmp[43]),
                                      "UP_L_ADULT4"       => trim($tmp[44]),
                                      "UP_L_ADULT5"       => trim($tmp[45]),
                                      "UP_L_ADULT6"       => trim($tmp[46]),
                                      "UP_L_ADULT7"       => trim($tmp[47]),
                                      "UP_L_ADULT8"       => trim($tmp[48]),
                                      "LW_R_ADULT8"       => trim($tmp[49]),
                                      "LW_R_ADULT7"       => trim($tmp[50]),
                                      "LW_R_ADULT6"       => trim($tmp[51]),
                                      "LW_R_ADULT5"       => trim($tmp[52]),
                                      "LW_R_ADULT4"       => trim($tmp[53]),
                                      "LW_R_ADULT3"       => trim($tmp[54]),
                                      "LW_R_ADULT2"       => trim($tmp[55]),
                                      "LW_R_ADULT1"       => trim($tmp[56]),
                                      "LW_L_ADULT1"       => trim($tmp[57]),
                                      "LW_L_ADULT2"       => trim($tmp[58]),
                                      "LW_L_ADULT3"       => trim($tmp[59]),
                                      "LW_L_ADULT4"       => trim($tmp[60]),
                                      "LW_L_ADULT5"       => trim($tmp[61]),
                                      "LW_L_ADULT6"       => trim($tmp[62]),
                                      "LW_L_ADULT7"       => trim($tmp[63]),
                                      "LW_L_ADULT8"       => trim($tmp[64]),
                                      "ADULTTOOTH"        => trim($tmp[65]),
                                      "REMAINADULTTOOTH"  => trim($tmp[66]),
                                      "TREATEDADULTTOOTH" => trim($tmp[67]),
                                      "LOSTADULTTOOTH"    => trim($tmp[68]),
                                      "BRACK_ADULTTOOTH"  => trim($tmp[69]),
                                      "OTHERDISEASECD"    => trim($tmp[70]),
                                      "OTHERDISEASE"      => trim($tmp[71]),
                                      "DENTISTREMARKCD"   => trim($tmp[72]),
                                      "DENTISTREMARK"     => trim($tmp[73]),
                                      "DENTISTREMARKDATE" => str_replace("/", "-", trim($tmp[74])),
                                      "DENTISTTREAT"      => trim($tmp[75]));
                        $data["ORTHODONTICS"] = $data["ORTHODONTICS"] ? $data["ORTHODONTICS"] : 0; //矯正の有無(初期値を0とする)
                    }

                    if ($data["DATE"] != "" && $this->dateRep($data["DATE"])) {
                        $data["DATE"] = $this->dateRep($data["DATE"]);
                    }
                    if ($this->field["CSVCD"] == "1") {
                        if ($data["TB_DATE"] != "" && $this->dateRep($data["TB_DATE"])) {
                            $data["TB_DATE"] = $this->dateRep($data["TB_DATE"]);
                        }
                        if ($data["TB_BCGDATE"] != "" && $this->dateRep($data["TB_BCGDATE"])) {
                            $data["TB_BCGDATE"] = $this->dateRep($data["TB_BCGDATE"]);
                        }
                        if ($data["TB_FILMDATE"] != "" && $this->dateRep($data["TB_FILMDATE"])) {
                            $data["TB_FILMDATE"] = $this->dateRep($data["TB_FILMDATE"]);
                        }
                        if ($data["DOC_DATE"] != "" && $this->dateRep($data["DOC_DATE"])) {
                            $data["DOC_DATE"] = $this->dateRep($data["DOC_DATE"]);
                        }
                    } else {
                        if ($data["DENTISTREMARKDATE"] != "" && $this->dateRep($data["DENTISTREMARKDATE"])) {
                            $data["DENTISTREMARKDATE"] = $this->dateRep($data["DENTISTREMARKDATE"]);
                        }
                    }

                    //０埋め
                    $data = $this->zeroUme($data);
                    //エラーチェック
                    if ($this->errCheck($db, $data, $checkdata, $record_no, $duplicateCsv1, $duplicateCsv2, $primaryKey1, $primaryKey2)) {
                        $tempArr[$i] = $data;
                        $i++;
                    }
                    foreach ($primaryKey1 as $key => $val) {
                        $duplicateCsv1[$record_no][$val] = $data[$val];
                    }
                    foreach ($primaryKey2 as $key => $val) {
                        $duplicateCsv2[$record_no][$val] = $data[$val];
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
    public function errCheck($db, $data, $checkdata, $line, $duplicateCsv1, $duplicateCsv2, $primaryKey1, $primaryKey2)
    {
        $returnFlg          = true;
        $hissuErrFlg        = false;    //必須エラーフラグ
        $numericErrFlg      = false;    //数値エラーフラグ
        $lengthErrFlg       = false;    //レングスエラーフラグ
        $masterErrFlg       = false;    //存在エラーフラグ
        $valuesErrFlg       = false;    //値エラーフラグ
        $existenceErrFlg    = false;    //存在エラーフラグ
        $duplicateCsvErrFlg = false;    //重複(CSV)エラーフラグ
        $otherErrFlg        = false;    //その他エラーフラグ

        $hissuErrMsg        = "";       //必須エラーメッセージ
        $numericErrMsg      = "";       //数値エラーメッセージ
        $lengthErrMsg       = "";       //レングスエラーメッセージ
        $masterErrMsg       = "";       //存在エラーメッセージ
        $valuesErrMsg       = "";       //値エラーメッセージ
        $existenceErrMsg    = "";       //存在エラーメッセージ
        $duplicateCsvErrMsg = "";       //重複(CSV)メッセージ
        $otherErrMsg        = "";       //その他エラーメッセージ

        $hissuSep        = "";
        $numericSep      = "";
        $lengthSep       = "";
        $masterSep       = "";
        $valuesSep       = "";
        $existenceSep    = "";
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
                        in_array($key, array("YEAR", "SCHREGNO", "R_EAR", "R_EAR_DB", "L_EAR", "L_EAR_DB", "ALBUMINURIA1CD", "URICSUGAR1CD", "URICBLEED1CD", "ALBUMINURIA2CD"
                        , "URICSUGAR2CD", "URICBLEED2CD", "URI_ADVISECD", "NUTRITIONCD", "SPINERIBCD", "EYEDISEASECD", "NOSEDISEASECD", "SKINDISEASECD", "HEART_MEDEXAM"
                        , "HEARTDISEASECD", "MANAGEMENT_DIV", "TB_RESULT", "TB_REMARKCD", "TB_OTHERTESTCD", "TB_NAMECD", "TB_ADVISECD", "PARASITE", "OTHERDISEASECD", "OTHER_ADVISECD"
                        , "DOC_CD", "TREATCD", "GUIDE_DIV", "JOINING_SPORTS_CLUB", "MEDICAL_HISTORY1", "MEDICAL_HISTORY2", "MEDICAL_HISTORY3")) && $data[$key] != "" && !is_numeric($data[$key])
                    ) {
                        $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                    }

                    if (
                        in_array($key, array("HEIGHT", "WEIGHT", "SITHEIGHT", "CHEST", "R_BAREVISION", "L_BAREVISION", "R_VISION", "L_VISION", "TB_REACT", "HEMOGLOBIN")) && $data[$key] != ""
                        && !is_numeric(str_replace(".", "", $data[$key]))
                    ) {
                        $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                    }

                    //値チェック
                    if (in_array($key, array("HEIGHT", "WEIGHT", "SITHEIGHT"))) {
                        if ($data[$key] != "" && !$this->ataiCheck($data[$key], 3, 1)) {
                            $returnFlg = $this->errSet($valuesErrFlg, $valuesErrMsg, "※値不正:", $valuesSep, $val[1]);
                        }
                    }
                    if (in_array($key, array("R_BAREVISION", "L_BAREVISION", "R_VISION", "L_VISION"))) {
                        if ($data[$key] != "" && !$this->ataiCheck($data[$key], 1, 2)) {
                            $returnFlg = $this->errSet($valuesErrFlg, $valuesErrMsg, "※値不正:", $valuesSep, $val[1]);
                        }
                    }
                    if (in_array($key, array("TB_REACT", "HEMOGLOBIN"))) {
                        if ($data[$key] != "" && !$this->ataiCheck($data[$key], 2, 1)) {
                            $returnFlg = $this->errSet($valuesErrFlg, $valuesErrMsg, "※値不正:", $valuesSep, $val[1]);
                        }
                    }
                    //文字数チェック
                    if ($data[$key] != "" && strlen($data[$key]) > $val[0]) {
                        $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                    }
                    //視力の文字チェック
                    if (in_array($key, array("R_BAREVISION_MARK"))) {
                        $err = $this->siryokumojiCheck($data["R_BAREVISION_MARK"], $data["R_BAREVISION"], $db);
                        if ($err) {
                            $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, "未対応の文字(視力)");
                        }
                    }
                    if (in_array($key, array("L_BAREVISION_MARK"))) {
                        $err = $this->siryokumojiCheck($data["L_BAREVISION_MARK"], $data["L_BAREVISION"], $db);
                        if ($err) {
                            $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, "未対応の文字(視力)");
                        }
                    }
                    if (in_array($key, array("R_VISION_MARK"))) {
                        $err = $this->siryokumojiCheck($data["R_VISION_MARK"], $data["R_VISION"], $db);
                        if ($err) {
                            $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, "未対応の文字(視力)");
                        }
                    }
                    if (in_array($key, array("L_VISION_MARK"))) {
                        $err = $this->siryokumojiCheck($data["L_VISION_MARK"], $data["L_VISION"], $db);
                        if ($err) {
                            $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, "未対応の文字(視力)");
                        }
                    }

                //歯・口腔-----
                } else {
                    //必須チェック
                    if (in_array($key, array("YEAR", "DATE", "SCHREGNO"))) {
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
                        in_array($key, array("YEAR", "SCHREGNO", "JAWS_JOINTCD", "JAWS_JOINTCD2", "PLAQUECD", "GUMCD", "ORTHODONTICS", "UP_R_BABY5", "UP_R_BABY4", "UP_R_BABY3"
                        , "UP_R_BABY2", "UP_R_BABY1", "UP_L_BABY1", "UP_L_BABY2", "UP_L_BABY3", "UP_L_BABY4", "UP_L_BABY5", "LW_R_BABY5", "LW_R_BABY4", "LW_R_BABY3", "LW_R_BABY2"
                        , "LW_R_BABY1", "LW_L_BABY1", "LW_L_BABY2", "LW_L_BABY3", "LW_L_BABY4", "LW_L_BABY5", "BABYTOOTH", "REMAINBABYTOOTH", "TREATEDBABYTOOTH", "BRACK_BABYTOOTH"
                        , "UP_R_ADULT8", "UP_R_ADULT7", "UP_R_ADULT6", "UP_R_ADULT5", "UP_R_ADULT4", "UP_R_ADULT3", "UP_R_ADULT2", "UP_R_ADULT1", "UP_L_ADULT1", "UP_L_ADULT2"
                        , "UP_L_ADULT3", "UP_L_ADULT4", "UP_L_ADULT5", "UP_L_ADULT6", "UP_L_ADULT7", "UP_L_ADULT8", "LW_R_ADULT8", "LW_R_ADULT7", "LW_R_ADULT6", "LW_R_ADULT5", "LW_R_ADULT4"
                        , "LW_R_ADULT3", "LW_R_ADULT2", "LW_R_ADULT1", "LW_L_ADULT1", "LW_L_ADULT2", "LW_L_ADULT3", "LW_L_ADULT4", "LW_L_ADULT5", "LW_L_ADULT6", "LW_L_ADULT7", "LW_L_ADULT8"
                        , "ADULTTOOTH", "REMAINADULTTOOTH", "TREATEDADULTTOOTH", "LOSTADULTTOOTH", "BRACK_ADULTTOOTH", "OTHERDISEASECD", "DENTISTREMARKCD"))
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
                if (in_array($key, array("YEAR", "DATE", "SCHREGNO"))) {
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
                if (in_array($key, array("YEAR", "DATE", "SCHREGNO"))) {
                    if (strlen($data[$key]) > $val[0]) {
                        $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                    }
                }
            }
        }

        if ($returnFlg) {
            foreach ($checkdata as $key => $val) {
                //日付の書式チェック
                if (in_array($key, array("DATE"))) {
                    if ($data[$key] != "" && !$this->dateRep($data[$key])) {
                        $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※日付:", $numericSep, $val[1]);
                    }
                }
                if ($this->field["CSVCD"] == "1") {
                    //日付の書式チェック
                    if (in_array($key, array("TB_DATE","TB_BCGDATE", "TB_FILMDATE", "DOC_DATE"))) {
                        if ($data[$key] != "" && !$this->dateRep($data[$key])) {
                            $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※日付:", $numericSep, $val[1]);
                        }
                    }
                //歯・口腔-----
                } else {
                    //日付の書式チェック
                    if (in_array($key, array("DENTISTREMARKDATE"))) {
                        if ($data[$key] != "" && !$this->dateRep($data[$key])) {
                            $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※日付:", $numericSep, $val[1]);
                        }
                    }
                }

                if ($this->field["SHORI_MEI"] == "1" && $data[$key] != "" && $val[2] != "") {
                    $query = knjf140Query::getMasterCheck($data, "1", $val[2], $data[$key]);
                    $mark = $db->getOne($query);
                    if ($mark < 1) {
                        $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, $val[1]."(".$val[2].")");
                    }
                }
            }
            //存在チェック(更新時のみ)
            if ($this->field["SHORI_MEI"] == "1" && $data["YEAR"] != "" && $data["SCHREGNO"] != "") {
                $query = knjf140Query::getMasterCheck($data, "2");
                if ($db->getOne($query) < 1) {
                    $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, "在籍データ");
                }
            }
        }
        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$hissuErrMsg.$numericErrMsg.$valuesErrMsg.$masterErrMsg.$lengthErrMsg.$existenceErrMsg.$otherErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knjf140Query::insertQueryErr($db, $line, $checkError);
        }
        return $returnFlg;
    }

    //ＣＳＶ出力
    public function getDownloadModel()
    {
        //項目
        $header_err = array("MSGROW"            => "行番号",
                            "MSGREMARK"         => "メッセージ");
        if ($this->field["CSVCD"] == "1") { //一般
            $header = array("CSVCD"                     => "※CSVCD",
                            "YEAR"                      => "※年度",
                            "DATE"                      => "※健診実施日",
                            "SCHREGNO"                  => "※学籍番号",
                            "HEIGHT"                    => "身長",
                            "WEIGHT"                    => "体重",
                            "SITHEIGHT"                 => "座高",
                            "CHEST"                     => "胸囲",
                            "R_BAREVISION"              => "視力右裸眼(数字)",
                            "R_BAREVISION_MARK"         => "視力右裸眼(文字)",
                            "L_BAREVISION"              => "視力左裸眼(数字)",
                            "L_BAREVISION_MARK"         => "視力左裸眼(文字)",
                            "R_VISION"                  => "視力右矯正(数字)",
                            "R_VISION_MARK"             => "視力右矯正(文字)",
                            "L_VISION"                  => "視力左矯正(数字)",
                            "L_VISION_MARK"             => "視力左矯正(文字)",
                            "R_EAR"                     => "聴力右状態コード",
                            "R_EAR_DB"                  => "聴力右DB",
                            "L_EAR"                     => "聴力左状態コード",
                            "L_EAR_DB"                  => "聴力左DB",
                            "ALBUMINURIA1CD"            => "一次尿蛋白コード",
                            "URICSUGAR1CD"              => "一次尿糖コード",
                            "URICBLEED1CD"              => "一次尿潜血コード",
                            "ALBUMINURIA2CD"            => "二次尿蛋白コード",
                            "URICSUGAR2CD"              => "二次尿糖コード",
                            "URICBLEED2CD"              => "二次尿潜血コード",
                            "URICOTHERTEST"             => "尿その他の検査",
                            "URI_ADVISECD"              => "尿その他の検査 指導区分",//指導区分 F021
                            "NUTRITIONCD"               => "栄養状態コード",
                            "SPINERIBCD"                => "脊柱胸郭コード",
                            "SPINERIBCD_REMARK"         => "脊柱胸郭所見",
                            "EYEDISEASECD"              => "目疾病及異常コード",
                            "EYE_TEST_RESULT"           => "眼科所見",
                            "NOSEDISEASECD"             => "耳鼻咽頭疾患コード",
                            "NOSEDISEASECD_REMARK"      => "耳鼻咽頭疾患所見",
                            "SKINDISEASECD"             => "皮膚疾患コード",
                            "HEART_MEDEXAM"             => "心臓臨床医学的検査コード",
                            "HEART_MEDEXAM_REMARK"      => "心臓臨床医学的検査",
                            "HEARTDISEASECD"            => "心臓疾病及異常コード",
                            "HEARTDISEASECD_REMARK"     => "心臓疾病及異常所見",
                            "MANAGEMENT_DIV"            => "心臓管理区分",
                            "TB_DATE"                   => "結核ツ反実施日",
                            "TB_REACT"                  => "結核ツ反応",
                            "TB_RESULT"                 => "結核ツ反判定コード",
                            "TB_BCGDATE"                => "結核BCG検査日",
                            "TB_FILMDATE"               => "結核撮影日付",
                            "TB_FILMNO"                 => "結核フィルム番号",
                            "TB_REMARKCD"               => "結核所見コード",
                            "TB_OTHERTESTCD"            => "結核その他検査コード",
                            "TB_NAMECD"                 => "結核病名コード",
                            "TB_ADVISECD"               => "結核指導区分コード",
                            "TB_X_RAY"                  => "結核所見(X線)",
                            "ANEMIA_REMARK"             => "貧血所見",
                            "HEMOGLOBIN"                => "貧血ヘモグロビン値",
                            "PARASITE"                  => "寄生虫卵",
                            "OTHERDISEASECD"            => "その他疾病及異常コード",
                            "OTHER_ADVISECD"            => "その他疾病及異常 指導区分",//指導区分 F145
                            "DOC_CD"                    => "学校医コード(内科検診)",
                            "DOC_REMARK"                => "学校医所見",
                            "DOC_DATE"                  => "学校医日付",
                            "TREATCD"                   => "事後処置コード",
                            "REMARK"                    => "備考",
                            "NUTRITION_RESULT"          => "内科検診結果(栄養状態)",
                            "EYEDISEASE_RESULT"         => "眼科検診結果(目疾病及異常)",
                            "SKINDISEASE_RESULT"        => "皮膚科検診結果(皮膚疾患)",
                            "SPINERIB_RESULT"           => "脊柱胸郭検診結果(脊柱胸郭)",
                            "NOSEDISEASE_RESULT"        => "耳鼻科検診結果(耳鼻咽頭疾患)",
                            "OTHERDISEASE_RESULT"       => "その他検診結果(その他疾病及異常)",
                            "HEARTDISEASE_RESULT"       => "心臓検診結果(心臓疾病及異常)",
                            "GUIDE_DIV"                 => "指導区分",
                            "JOINING_SPORTS_CLUB"       => "運動部活動",
                            "MEDICAL_HISTORY1"          => "既往症1",
                            "MEDICAL_HISTORY2"          => "既往症2",
                            "MEDICAL_HISTORY3"          => "既往症3",
                            "DIAGNOSIS_NAME"            => "診断名",
                            $this->lastColumn           => $this->lastColumn);
            $header_rei = array("CSVCD"                     => "1",
                                "YEAR"                      => "2005",
                                "DATE"                      => "2005/04/15",
                                "SCHREGNO"                  => "20055001",
                                "HEIGHT"                    => "165.5",
                                "WEIGHT"                    => "80.5",
                                "SITHEIGHT"                 => "75.5",
                                "CHEST"                     => "70.5",
                                "R_BAREVISION"              => "0.50",
                                "R_BAREVISION_MARK"         => "C",
                                "L_BAREVISION"              => "0.70",
                                "L_BAREVISION_MARK"         => "B",
                                "R_VISION"                  => "1.50",
                                "R_VISION_MARK"             => "A",
                                "L_VISION"                  => "1.70",
                                "L_VISION_MARK"             => "A",
                                "R_EAR"                     => "01",
                                "R_EAR_DB"                  => "20",
                                "L_EAR"                     => "02",
                                "L_EAR_DB"                  => "15",
                                "ALBUMINURIA1CD"            => "01",
                                "URICSUGAR1CD"              => "01",
                                "URICBLEED1CD"              => "01",
                                "ALBUMINURIA2CD"            => "01",
                                "URICSUGAR2CD"              => "01",
                                "URICBLEED2CD"              => "01",
                                "URICOTHERTEST"             => "再検査済　異常なし",
                                "URI_ADVISECD"              => "01",
                                "NUTRITIONCD"               => "01",
                                "SPINERIBCD"                => "01",
                                "SPINERIBCD_REMARK"         => "脊柱、胸郭　異常なし",
                                "EYEDISEASECD"              => "01",
                                "EYE_TEST_RESULT"           => "知覚障害",
                                "NOSEDISEASECD"             => "01",
                                "NOSEDISEASECD_REMARK"      => "急性中耳炎",
                                "SKINDISEASECD"             => "01",
                                "HEART_MEDEXAM"             => "01",
                                "HEART_MEDEXAM_REMARK"      => "不完全右脚ブロック、心雑音、不整脈など",
                                "HEARTDISEASECD"            => "01",
                                "HEARTDISEASECD_REMARK"     => "心不全",
                                "MANAGEMENT_DIV"            => "01",
                                "TB_DATE"                   => "2005/04/15",
                                "TB_REACT"                  => "20.0",
                                "TB_RESULT"                 => "02",
                                "TB_BCGDATE"                => "2005/04/15",
                                "TB_FILMDATE"               => "2005/04/15",
                                "TB_FILMNO"                 => "F55030",
                                "TB_REMARKCD"               => "01",
                                "TB_OTHERTESTCD"            => "01",
                                "TB_NAMECD"                 => "01",
                                "TB_ADVISECD"               => "01",
                                "TB_X_RAY"                  => "異常なし",
                                "ANEMIA_REMARK"             => "食事療法",
                                "HEMOGLOBIN"                => "10.5",
                                "PARASITE"                  => "01",
                                "OTHERDISEASECD"            => "01",
                                "OTHER_ADVISECD"            => "01",
                                "DOC_CD"                    => "01",
                                "DOC_REMARK"                => "食事療法",
                                "DOC_DATE"                  => "2005/05/01",
                                "TREATCD"                   => "01",
                                "REMARK"                    => "食事療法",
                                "NUTRITION_RESULT"          => "異常なし",
                                "EYEDISEASE_RESULT"         => "異常なし",
                                "SKINDISEASE_RESULT"        => "異常なし",
                                "SPINERIB_RESULT"           => "異常なし",
                                "NOSEDISEASE_RESULT"        => "異常なし",
                                "OTHERDISEASE_RESULT"       => "異常なし",
                                "HEARTDISEASE_RESULT"       => "異常なし",
                                "GUIDE_DIV"                 => "1",
                                "JOINING_SPORTS_CLUB"       => "1",
                                "MEDICAL_HISTORY1"          => "100",
                                "MEDICAL_HISTORY2"          => "100",
                                "MEDICAL_HISTORY3"          => "100",
                                "DIAGNOSIS_NAME"            => "自閉症",
                                $this->lastColumn           => $this->lastColumn);
        } else { //歯・口腔
            $header = array("CSVCD"             => "※CSVCD",
                            "YEAR"              => "※年度",
                            "DATE"              => "※健診実施日",
                            "SCHREGNO"          => "※学籍番号",
                            "JAWS_JOINTCD"      => "歯列咬合顎関節コード",
                            "JAWS_JOINTCD2"     => "顎関節コード",
                            "PLAQUECD"          => "歯垢状態コード",
                            "GUMCD"             => "歯肉状態コード",
                            "ORTHODONTICS"      => "矯正の有無",
                            "UP_R_BABY5"        => "乳歯 上 右E",
                            "UP_R_BABY4"        => "乳歯 上 右D",
                            "UP_R_BABY3"        => "乳歯 上 右C",
                            "UP_R_BABY2"        => "乳歯 上 右B",
                            "UP_R_BABY1"        => "乳歯 上 右A",
                            "UP_L_BABY1"        => "乳歯 上 左A",
                            "UP_L_BABY2"        => "乳歯 上 左B",
                            "UP_L_BABY3"        => "乳歯 上 左C",
                            "UP_L_BABY4"        => "乳歯 上 左D",
                            "UP_L_BABY5"        => "乳歯 上 左E",
                            "LW_R_BABY5"        => "乳歯 下 右E",
                            "LW_R_BABY4"        => "乳歯 下 右D",
                            "LW_R_BABY3"        => "乳歯 下 右C",
                            "LW_R_BABY2"        => "乳歯 下 右B",
                            "LW_R_BABY1"        => "乳歯 下 右A",
                            "LW_L_BABY1"        => "乳歯 下 左A",
                            "LW_L_BABY2"        => "乳歯 下 左B",
                            "LW_L_BABY3"        => "乳歯 下 左C",
                            "LW_L_BABY4"        => "乳歯 下 左D",
                            "LW_L_BABY5"        => "乳歯 下 左E",
                            "BABYTOOTH"         => "乳歯現在数",
                            "REMAINBABYTOOTH"   => "乳歯未処置数",
                            "TREATEDBABYTOOTH"  => "乳歯処置数",
                            "BRACK_BABYTOOTH"   => "要注意乳歯数",
                            "UP_R_ADULT8"       => "永久歯 上 右8",
                            "UP_R_ADULT7"       => "永久歯 上 右7",
                            "UP_R_ADULT6"       => "永久歯 上 右6",
                            "UP_R_ADULT5"       => "永久歯 上 右5",
                            "UP_R_ADULT4"       => "永久歯 上 右4",
                            "UP_R_ADULT3"       => "永久歯 上 右3",
                            "UP_R_ADULT2"       => "永久歯 上 右2",
                            "UP_R_ADULT1"       => "永久歯 上 右1",
                            "UP_L_ADULT1"       => "永久歯 上 左1",
                            "UP_L_ADULT2"       => "永久歯 上 左2",
                            "UP_L_ADULT3"       => "永久歯 上 左3",
                            "UP_L_ADULT4"       => "永久歯 上 左4",
                            "UP_L_ADULT5"       => "永久歯 上 左5",
                            "UP_L_ADULT6"       => "永久歯 上 左6",
                            "UP_L_ADULT7"       => "永久歯 上 左7",
                            "UP_L_ADULT8"       => "永久歯 上 左8",
                            "LW_R_ADULT8"       => "永久歯 下 右8",
                            "LW_R_ADULT7"       => "永久歯 下 右7",
                            "LW_R_ADULT6"       => "永久歯 下 右6",
                            "LW_R_ADULT5"       => "永久歯 下 右5",
                            "LW_R_ADULT4"       => "永久歯 下 右4",
                            "LW_R_ADULT3"       => "永久歯 下 右3",
                            "LW_R_ADULT2"       => "永久歯 下 右2",
                            "LW_R_ADULT1"       => "永久歯 下 右1",
                            "LW_L_ADULT1"       => "永久歯 下 左1",
                            "LW_L_ADULT2"       => "永久歯 下 左2",
                            "LW_L_ADULT3"       => "永久歯 下 左3",
                            "LW_L_ADULT4"       => "永久歯 下 左4",
                            "LW_L_ADULT5"       => "永久歯 下 左5",
                            "LW_L_ADULT6"       => "永久歯 下 左6",
                            "LW_L_ADULT7"       => "永久歯 下 左7",
                            "LW_L_ADULT8"       => "永久歯 下 左8",
                            "ADULTTOOTH"        => "永久歯数",
                            "REMAINADULTTOOTH"  => "永久歯未処置数",
                            "TREATEDADULTTOOTH" => "永久歯処置数",
                            "LOSTADULTTOOTH"    => "永久歯喪失数",
                            "BRACK_ADULTTOOTH"  => "要観察歯数",
                            "OTHERDISEASECD"    => "その他疾病及び異常コード",
                            "OTHERDISEASE"      => "その他疾病及び異常",
                            "DENTISTREMARKCD"   => "学校歯科医所見コード",
                            "DENTISTREMARK"     => "学校医処理",
                            "DENTISTREMARKDATE" => "学校歯科医所見日付",
                            "DENTISTTREAT"      => "学校歯科医事後処置",
                            $this->lastColumn   => $this->lastColumn);
            $header_rei = array("CSVCD"             => "2",
                                "YEAR"              => "2005",
                                "DATE"              => "2005/04/15",
                                "SCHREGNO"          => "20055001",
                                "JAWS_JOINTCD"      => "01",
                                "JAWS_JOINTCD2"     => "01",
                                "PLAQUECD"          => "02",
                                "GUMCD"             => "01",
                                "ORTHODONTICS"      => "1",
                                "UP_R_BABY5"        => "01",
                                "UP_R_BABY4"        => "02",
                                "UP_R_BABY3"        => "03",
                                "UP_R_BABY2"        => "04",
                                "UP_R_BABY1"        => "05",
                                "UP_L_BABY1"        => "06",
                                "UP_L_BABY2"        => "01",
                                "UP_L_BABY3"        => "02",
                                "UP_L_BABY4"        => "03",
                                "UP_L_BABY5"        => "04",
                                "LW_R_BABY5"        => "05",
                                "LW_R_BABY4"        => "06",
                                "LW_R_BABY3"        => "01",
                                "LW_R_BABY2"        => "02",
                                "LW_R_BABY1"        => "03",
                                "LW_L_BABY1"        => "04",
                                "LW_L_BABY2"        => "05",
                                "LW_L_BABY3"        => "06",
                                "LW_L_BABY4"        => "01",
                                "LW_L_BABY5"        => "02",
                                "BABYTOOTH"         => "8",
                                "REMAINBABYTOOTH"   => "1",
                                "TREATEDBABYTOOTH"  => "2",
                                "BRACK_BABYTOOTH"   => "5",
                                "UP_R_ADULT8"       => "01",
                                "UP_R_ADULT7"       => "02",
                                "UP_R_ADULT6"       => "03",
                                "UP_R_ADULT5"       => "04",
                                "UP_R_ADULT4"       => "05",
                                "UP_R_ADULT3"       => "06",
                                "UP_R_ADULT2"       => "01",
                                "UP_R_ADULT1"       => "02",
                                "UP_L_ADULT1"       => "03",
                                "UP_L_ADULT2"       => "04",
                                "UP_L_ADULT3"       => "05",
                                "UP_L_ADULT4"       => "06",
                                "UP_L_ADULT5"       => "01",
                                "UP_L_ADULT6"       => "02",
                                "UP_L_ADULT7"       => "03",
                                "UP_L_ADULT8"       => "04",
                                "LW_R_ADULT8"       => "05",
                                "LW_R_ADULT7"       => "06",
                                "LW_R_ADULT6"       => "01",
                                "LW_R_ADULT5"       => "02",
                                "LW_R_ADULT4"       => "03",
                                "LW_R_ADULT3"       => "04",
                                "LW_R_ADULT2"       => "05",
                                "LW_R_ADULT1"       => "06",
                                "LW_L_ADULT1"       => "01",
                                "LW_L_ADULT2"       => "02",
                                "LW_L_ADULT3"       => "03",
                                "LW_L_ADULT4"       => "04",
                                "LW_L_ADULT5"       => "05",
                                "LW_L_ADULT6"       => "06",
                                "LW_L_ADULT7"       => "01",
                                "LW_L_ADULT8"       => "02",
                                "ADULTTOOTH"        => "20",
                                "REMAINADULTTOOTH"  => "3",
                                "TREATEDADULTTOOTH" => "5",
                                "LOSTADULTTOOTH"    => "2",
                                "BRACK_ADULTTOOTH"  => "3",
                                "OTHERDISEASECD"    => "02",
                                "OTHERDISEASE"      => "全角20文字（コードが99のみ入力可）",
                                "DENTISTREMARKCD"   => "02",
                                "DENTISTREMARK"     => "未使用です",
                                "DENTISTREMARKDATE" => "2005/04/15",
                                "DENTISTTREAT"      => "治療完了",
                                $this->lastColumn   => $this->lastColumn);
        }

        $contents  = ""; //クリア

        if ($this->field["OUTPUT"]=="1") { //ヘッダ
            $contents .= implode($header, ",")."\n";
            $contents .= implode($header_rei, ",")."\n";
        } elseif ($this->field["OUTPUT"] == "3") {        //エラー
            if ($this->field["CSVCD"] == "1") {
                $tables = array("1" => "MEDEXAM_DET_DAT", "2" => "MEDEXAM_HDAT");
            } else {
                $tables = array("1" => "MEDEXAM_TOOTH_DAT", "2" => "MEDEXAM_HDAT");
            }
            $dupTable = $this->getDuplicateErrorHeader(PROGRAMID, $tables);
            if (sizeof($dupTable) > 0) {
                $headerErr = $this->duplicateCsvHeader($dupTable, $header);
                $contents .= implode($headerErr, ",")."\n";
            }
            $contents .= implode($header_err, ",")."\n";
        } elseif ($this->field["OUTPUT"]=="4") {        //データ
            if ($this->field["HEADER"]=="on") {
                $contents .= implode($header, ",")."\n";
            }
        }
        if ($this->field["OUTPUT"]=="1") { //ヘッダ
            $header_data = true;
        } else { //ヘッダ以外
            $header_data = false;
            //DB接続
            $db = Query::dbCheckOut();
            if ($this->field["OUTPUT"]=="3") { //エラー
                $query = $this->selectCsvErrQuery(PROGRAMID);
            } elseif ($this->field["OUTPUT"]=="4") { //データ
                $query = knjf140Query::getselectDataSQL($this);
            }
            //SQL文発行
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($this->field["OUTPUT"]=="4") { //データ
                    $row = str_replace(",", ".", $row);//データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                }
                $contents .= implode($row, ",") ."\n";
                $header_data = true;
            }
            $result->free();
            Query::dbCheckIn($db);
        }
        //ＣＳＶ出力
        if ($header_data) {
            if ($this->field["OUTPUT"]=="3") {        //エラー
                $couse = "エラー出力";
            } else {                                //エラー以外
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
            //一般-----
            //歯・口腔-----
            if ($data["SCHREGNO"] != "") {
                if (strlen($data["SCHREGNO"]) < 8 && is_numeric($data["SCHREGNO"])) {
                    $data["SCHREGNO"] = sprintf("%08d", $data["SCHREGNO"]);
                }
            }

            //一般-----
            if ($this->field["CSVCD"] == "1") {
                if ($data["R_BAREVISION"] != "") {
                    $data_arr = array();
                    $data_arr = explode(".", $data["R_BAREVISION"]);
                    if (is_numeric($data_arr[0]) && is_numeric($data_arr[1])) {
                        $data["R_BAREVISION"] = $data_arr[0] . "." . sprintf("%-02s", $data_arr[1]);
                    }
                }
                if ($data["L_BAREVISION"] != "") {
                    $data_arr = array();
                    $data_arr = explode(".", $data["L_BAREVISION"]);
                    if (is_numeric($data_arr[0]) && is_numeric($data_arr[1])) {
                        $data["L_BAREVISION"] = $data_arr[0] . "." . sprintf("%-02s", $data_arr[1]);
                    }
                }
                if ($data["R_VISION"] != "") {
                    $data_arr = array();
                    $data_arr = explode(".", $data["R_VISION"]);
                    if (is_numeric($data_arr[0]) && is_numeric($data_arr[1])) {
                        $data["R_VISION"] = $data_arr[0] . "." . sprintf("%-02s", $data_arr[1]);
                    }
                }
                if ($data["L_VISION"] != "") {
                    $data_arr = array();
                    $data_arr = explode(".", $data["L_VISION"]);
                    if (is_numeric($data_arr[0]) && is_numeric($data_arr[1])) {
                        $data["L_VISION"] = $data_arr[0] . "." . sprintf("%-02s", $data_arr[1]);
                    }
                }
                if ($data["R_EAR"] != "") {
                    if (strlen($data["R_EAR"]) < 2 && is_numeric($data["R_EAR"])) {
                        $data["R_EAR"]            = sprintf("%02d", $data["R_EAR"]);
                    }
                }
                if ($data["L_EAR"] != "") {
                    if (strlen($data["L_EAR"]) < 2 && is_numeric($data["L_EAR"])) {
                        $data["L_EAR"]            = sprintf("%02d", $data["L_EAR"]);
                    }
                }
                if ($data["ALBUMINURIA1CD"] != "") {
                    if (strlen($data["ALBUMINURIA1CD"]) < 2 && is_numeric($data["ALBUMINURIA1CD"])) {
                        $data["ALBUMINURIA1CD"]   = sprintf("%02d", $data["ALBUMINURIA1CD"]);
                    }
                }
                if ($data["URICSUGAR1CD"] != "") {
                    if (strlen($data["URICSUGAR1CD"]) < 2 && is_numeric($data["URICSUGAR1CD"])) {
                        $data["URICSUGAR1CD"]     = sprintf("%02d", $data["URICSUGAR1CD"]);
                    }
                }
                if ($data["URICBLEED1CD"] != "") {
                    if (strlen($data["URICBLEED1CD"]) < 2 && is_numeric($data["URICBLEED1CD"])) {
                        $data["URICBLEED1CD"]     = sprintf("%02d", $data["URICBLEED1CD"]);
                    }
                }
                if ($data["ALBUMINURIA2CD"] != "") {
                    if (strlen($data["ALBUMINURIA2CD"]) < 2 && is_numeric($data["ALBUMINURIA2CD"])) {
                        $data["ALBUMINURIA2CD"]   = sprintf("%02d", $data["ALBUMINURIA2CD"]);
                    }
                }
                if ($data["URICSUGAR2CD"] != "") {
                    if (strlen($data["URICSUGAR2CD"]) < 2 && is_numeric($data["URICSUGAR2CD"])) {
                        $data["URICSUGAR2CD"]     = sprintf("%02d", $data["URICSUGAR2CD"]);
                    }
                }
                if ($data["URICBLEED2CD"] != "") {
                    if (strlen($data["URICBLEED2CD"]) < 2 && is_numeric($data["URICBLEED2CD"])) {
                        $data["URICBLEED2CD"]     = sprintf("%02d", $data["URICBLEED2CD"]);
                    }
                }
                if ($data["URI_ADVISECD"] != "") {
                    if (strlen($data["URI_ADVISECD"]) < 2 && is_numeric($data["URI_ADVISECD"])) {
                        $data["URI_ADVISECD"]     = sprintf("%02d", $data["URI_ADVISECD"]);
                    }
                }
                if ($data["NUTRITIONCD"] != "") {
                    if (strlen($data["NUTRITIONCD"]) < 2 && is_numeric($data["NUTRITIONCD"])) {
                        $data["NUTRITIONCD"]      = sprintf("%02d", $data["NUTRITIONCD"]);
                    }
                }
                if ($data["SPINERIBCD"] != "") {
                    if (strlen($data["SPINERIBCD"]) < 2 && is_numeric($data["SPINERIBCD"])) {
                        $data["SPINERIBCD"]       = sprintf("%02d", $data["SPINERIBCD"]);
                    }
                }
                if ($data["EYEDISEASECD"] != "") {
                    if (strlen($data["EYEDISEASECD"]) < 2 && is_numeric($data["EYEDISEASECD"])) {
                        $data["EYEDISEASECD"]     = sprintf("%02d", $data["EYEDISEASECD"]);
                    }
                }
                if ($data["NOSEDISEASECD"] != "") {
                    if (strlen($data["NOSEDISEASECD"]) < 2 && is_numeric($data["NOSEDISEASECD"])) {
                        $data["NOSEDISEASECD"]    = sprintf("%02d", $data["NOSEDISEASECD"]);
                    }
                }
                if ($data["SKINDISEASECD"] != "") {
                    if (strlen($data["SKINDISEASECD"]) < 2 && is_numeric($data["SKINDISEASECD"])) {
                        $data["SKINDISEASECD"]    = sprintf("%02d", $data["SKINDISEASECD"]);
                    }
                }
                if ($data["HEART_MEDEXAM"] != "") {
                    if (strlen($data["HEART_MEDEXAM"]) < 2 && is_numeric($data["HEART_MEDEXAM"])) {
                        $data["HEART_MEDEXAM"]    = sprintf("%02d", $data["HEART_MEDEXAM"]);
                    }
                }
                if ($data["HEARTDISEASECD"] != "") {
                    if (strlen($data["HEARTDISEASECD"]) < 2 && is_numeric($data["HEARTDISEASECD"])) {
                        $data["HEARTDISEASECD"]   = sprintf("%02d", $data["HEARTDISEASECD"]);
                    }
                }
                if ($data["MANAGEMENT_DIV"] != "") {
                    if (strlen($data["MANAGEMENT_DIV"]) < 2 && is_numeric($data["MANAGEMENT_DIV"])) {
                        $data["MANAGEMENT_DIV"]   = sprintf("%02d", $data["MANAGEMENT_DIV"]);
                    }
                }
                if ($data["PARASITE"] != "") {
                    if (strlen($data["PARASITE"]) < 2 && is_numeric($data["PARASITE"])) {
                        $data["PARASITE"]         = sprintf("%02d", $data["PARASITE"]);
                    }
                }
                if ($data["DOC_CD"] != "") {
                    if (strlen($data["DOC_CD"]) < 2 && is_numeric($data["DOC_CD"])) {
                        $data["DOC_CD"]           = sprintf("%02d", $data["DOC_CD"]);
                    }
                }
                if ($data["TB_RESULT"] != "") {
                    if (strlen($data["TB_RESULT"]) < 2 && is_numeric($data["TB_RESULT"])) {
                        $data["TB_RESULT"]        = sprintf("%02d", $data["TB_RESULT"]);
                    }
                }
                if ($data["TB_REMARKCD"] != "") {
                    if (strlen($data["TB_REMARKCD"]) < 2 && is_numeric($data["TB_REMARKCD"])) {
                        $data["TB_REMARKCD"]      = sprintf("%02d", $data["TB_REMARKCD"]);
                    }
                }
                if ($data["TB_OTHERTESTCD"] != "") {
                    if (strlen($data["TB_OTHERTESTCD"]) < 2 && is_numeric($data["TB_OTHERTESTCD"])) {
                        $data["TB_OTHERTESTCD"]   = sprintf("%02d", $data["TB_OTHERTESTCD"]);
                    }
                }
                if ($data["TB_NAMECD"] != "") {
                    if (strlen($data["TB_NAMECD"]) < 2 && is_numeric($data["TB_NAMECD"])) {
                        $data["TB_NAMECD"]        = sprintf("%02d", $data["TB_NAMECD"]);
                    }
                }
                if ($data["TB_ADVISECD"] != "") {
                    if (strlen($data["TB_ADVISECD"]) < 2 && is_numeric($data["TB_ADVISECD"])) {
                        $data["TB_ADVISECD"]      = sprintf("%02d", $data["TB_ADVISECD"]);
                    }
                }
                if ($data["OTHERDISEASECD"] != "") {
                    if (strlen($data["OTHERDISEASECD"]) < 2 && is_numeric($data["OTHERDISEASECD"])) {
                        $data["OTHERDISEASECD"]   = sprintf("%02d", $data["OTHERDISEASECD"]);
                    }
                }
                if ($data["OTHER_ADVISECD"] != "") {
                    if (strlen($data["OTHER_ADVISECD"]) < 2 && is_numeric($data["OTHER_ADVISECD"])) {
                        $data["OTHER_ADVISECD"]   = sprintf("%02d", $data["OTHER_ADVISECD"]);
                    }
                }
                if ($data["TREATCD"] != "") {
                    if (strlen($data["TREATCD"]) < 2 && is_numeric($data["TREATCD"])) {
                        $data["TREATCD"]          = sprintf("%02d", $data["TREATCD"]);
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
                if ($data["UP_R_ADULT7"]!="") {
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
                //学校歯科医所見コード
                if ($data["DENTISTREMARKCD"] != "") {
                    if (strlen($data["DENTISTREMARKCD"]) < 2 && is_numeric($data["DENTISTREMARKCD"])) {
                        $data["DENTISTREMARKCD"] = sprintf("%02d", $data["DENTISTREMARKCD"]);
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

    //視力の文字チェック
    public function siryokumojiCheck(&$mark, $suuji, $db)
    {
        if ($mark) {
            switch ($mark) {
                case "a":
                case "A":
                case "ａ":
                case "Ａ":
                    $mark = "A";
                    break;
                case "b":
                case "B":
                case "ｂ":
                case "Ｂ":
                    $mark = "B";
                    break;
                case "c":
                case "C":
                case "ｃ":
                case "Ｃ":
                    $mark = "C";
                    break;
                case "d":
                case "D":
                case "ｄ":
                case "Ｄ":
                    $mark = "D";
                    break;
                default:
                    return true;
                    break;
            }
        } elseif ($suuji) {
            $query = knjf140Query::getSiryokuMark($suuji); //視力も文字取得
            $mark = $db->getOne($query);
        }
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
