<?php

require_once('for_php7.php');
class knjf140aModel extends Model
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
    public $z010name1;
    public $lastColumn;//CSVの最後の列の値

    public function knjf140aModel()
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
                             "SCHOOL_KIND"    =>  VARS::post("SCHOOL_KIND"),    //校種
                             "YEAR"           =>  VARS::post("YEAR"),           //年度一覧
                             "GRADE_HR_CLASS" =>  VARS::post("GRADE_HR_CLASS"), //年組一覧
                             "HEADER"         =>  VARS::post("HEADER"),         //ヘッダ有
                             "CSVCD"          =>  VARS::post("CSVCD"),          //対象データ
                             "SHORI_MEI"      =>  VARS::post("SHORI_MEI")       //処理名
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
        $this->z010name1 = $db->getOne(knjf140aQuery::getSchoolName());
        Query::dbCheckIn($db);

        //福井県フラグ
        $this->isFukui = ($this->z010name1 == "fukuiken") ? true: false;

        //宮城県フラグ
        $this->isMigagi = ($this->z010name1 == "miyagiken") ? true: false;

        //広工大フラグ
        $this->isHirokoudai = ($this->z010name1 == "hirokoudai") ? true: false;

        //三重県フラグ
        $this->isMie = ($this->z010name1 == "mieken") ? true: false;

        //駒澤フラグ
        $this->isKoma = ($this->z010name1 == "koma") ? true: false;

        $this->maxOtherRemark2Byte = '60';
        $this->maxRemarkByte       = '180';
        $this->isShien = false;
        if ($this->Properties["useSpecial_Support_School"] == "1") { //特別支援学校
            $this->isShien = true;
            $this->maxOtherRemark2Byte = '300';
            $this->maxRemarkByte       = '300';
        }

        $this->tb_filmnoFieldSize = ($this->Properties["usetb_filmnoFieldSize"] > 0) ? $this->Properties["usetb_filmnoFieldSize"] : 6;

        $this->is_f020_otherdisese_hyouji = false;
        $this->is_f020_otherdisese_hyouji2 = false;
        $this->is_f020_dentistremark_hyouji = false;
        if ($this->Properties["printKenkouSindanIppan"] == "1") {
            if ($this->z010name1 != "miyagiken" && $this->z010name1 != "mieken") {
                //三重県、宮城県以外の場合
                if ($this->Properties["F020_OTHERDISESE_HYOUJI"] == "3") {
                    $this->is_f020_otherdisese_hyouji = true;
                }
                if ($this->Properties["F020_OTHERDISESE_HYOUJI2"] == "3") {
                    $this->is_f020_otherdisese_hyouji2 = true;
                }
                if ($this->Properties["F020_DENTISTREMARK_HYOUJI"] == "3") {
                    $this->is_f020_dentistremark_hyouji = true;
                }
            } else {
                $this->is_f020_otherdisese_hyouji = false;
                $this->is_f020_otherdisese_hyouji2 = false;
                $this->is_f020_dentistremark_hyouji = false;
            }
        }

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
                for ($i = 0; $i < get_count($this->dataArr); $i++) {
                    //更新
                    if ($this->field["SHORI_MEI"] == "1") {
                        //delete and insert
                        knjf140aQuery::deleteinsertQueryCsv($db, $this->dataArr[$i], $this->field["CSVCD"], $this);
                    //削除
                    } else {
                        //delete
                        knjf140aQuery::deleteQueryCsv($db, $this->dataArr[$i], $this->field["CSVCD"]);
                    }
                }
                $this->setMessage("MSG202", "更新件数".get_count($this->dataArr)."件");
                Query::dbCheckIn($db);
            }
        }
    }
    public function getFileString()
    {
        $tempArr = array();
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

        if (is_uploaded_file($this->field["FILE"]["tmp_name"])) { //HTTP POSTによりアップロードされたファイルかどうかを調べる
            $db = Query::dbCheckOut();
            $query = knjf140aQuery::isKindai();
            $isKindai = $db->getOne($query);
            $remarkLen = $isKindai == 1 ? 180 : $this->maxRemarkByte;
            if (in_array($this->Properties["printKenkouSindanIppan"], array("1", "2", "3"))) {
                $Ippan = $this->Properties["printKenkouSindanIppan"];
            } else {
                $Ippan = "0";
            }
            $checkdata = array();
            if ($this->field["CSVCD"] == "1") { //一般
                $checkdata["YEAR"]     = array(4,"年度");
                $checkdata["DATE"]     = array(10,"健診実施日");
                $checkdata["SCHREGNO"] = array(8,"学籍番号");
                $checkdata["HEIGHT"]   = array(5,"身長");
                $checkdata["WEIGHT"]   = array(5,"体重");
                if ($this->isFukui) {
                    $checkdata["NO_PRINT_OBESITY_INDEX"] = array(1,"帳票印字無し（肥満度）");
                } else {
                    if (!$this->isMigagi && $this->Properties["hideSitHeight"] != "1") {
                        $checkdata["SITHEIGHT"]          = array(5,"座高");
                    }
                }
                $checkdata["R_VISION_CANTMEASURE"]  = array(1,"視力(右)測定困難");
                $checkdata["L_VISION_CANTMEASURE"]  = array(1,"視力(左)測定困難");
                if ($this->isHirokoudai) {
                    $checkdata["R_BAREVISION"]      = array(5,"視力右裸眼(数字)");
                    $checkdata["L_BAREVISION"]      = array(5,"視力左裸眼(数字)");
                    $checkdata["R_VISION"]          = array(5,"視力右矯正(数字)");
                    $checkdata["L_VISION"]          = array(5,"視力左矯正(数字)");
                } else {
                    if ($Ippan == "0" || $this->isFukui) {
                        $checkdata["R_BAREVISION"]   = array(5,"視力右裸眼(数字)");
                    }
                    $checkdata["R_BAREVISION_MARK"]  = array(3,"視力右裸眼(文字)");
                    if ($Ippan == "0" || $this->isFukui) {
                        $checkdata["L_BAREVISION"]   = array(5,"視力左裸眼(数字)");
                    }
                    $checkdata["L_BAREVISION_MARK"]  = array(3,"視力左裸眼(文字)");
                    if ($Ippan == "0" || $this->isFukui) {
                        $checkdata["R_VISION"]       = array(5,"視力右矯正(数字)");
                    }
                    $checkdata["R_VISION_MARK"]      = array(3,"視力右矯正(文字)");
                    if ($Ippan == "0" || $this->isFukui) {
                        $checkdata["L_VISION"]       = array(5,"視力左矯正(数字)");
                    }
                    $checkdata["L_VISION_MARK"]      = array(3,"視力左矯正(文字)");
                }
                $setEarTitle = "平均dB";
                //広工大は文言変更
                if ($this->isHirokoudai) {
                    $setEarTitle = "1000Hz";
                }
                $checkdata["R_EAR_CANTMEASURE"]      = array(1,"聴力(右)測定困難");
                if (!$this->isFukui && ($this->isShien || $this->Properties["unEar_db"] != "1")) {
                    if ($this->isKoma || ($this->Properties["printKenkouSindanIppan"] == "1" && $this->Properties["KenkouSindan_Ippan_Pattern"] == "1")) {
                        $checkdata["R_EAR_DB_1000"]  = array(3,"聴力右1000Hz", "F010");
                    } else {
                        $checkdata["R_EAR_DB"]       = array(3,"聴力右".$setEarTitle);
                    }
                    if ($this->Properties["useEar4000Hz"] == "1") {
                        $checkdata["R_EAR_DB_4000"]  = array(3,"聴力右4000Hz", "F010");
                    }
                }
                if (!$this->isKoma && ($this->Properties["printKenkouSindanIppan"] != "1" || $this->Properties["KenkouSindan_Ippan_Pattern"] != "1")) {
                    $checkdata["R_EAR"]              = array(2,"聴力右状態コード", "F010");
                }
                if ($this->isShien) {
                    if (!$this->isFukui) {
                        $checkdata["R_EAR_DB_IN"]    = array(3,"(装用時)聴力右平均dB");
                        if ($this->Properties["useEar4000Hz"] == "1") {
                            $checkdata["R_EAR_DB_4000_IN"]  = array(3,"(装用時)聴力右4000Hz");
                        }
                    }
                    $checkdata["R_EAR_IN"]           = array(2,"(装用時)聴力右状態コード");
                }
                $checkdata["L_EAR_CANTMEASURE"]      = array(1,"聴力(左)測定困難");
                if (!$this->isFukui && ($this->isShien || $this->Properties["unEar_db"] != "1")) {
                    if ($this->isKoma || ($this->Properties["printKenkouSindanIppan"] == "1" && $this->Properties["KenkouSindan_Ippan_Pattern"] == "1")) {
                        $checkdata["L_EAR_DB_1000"]  = array(3,"聴力左1000Hz", "F010");
                    } else {
                        $checkdata["L_EAR_DB"]       = array(3,"聴力左".$setEarTitle);
                    }
                    if ($this->Properties["useEar4000Hz"] == "1") {
                        $checkdata["L_EAR_DB_4000"]  = array(3,"聴力左4000Hz", "F010");
                    }
                }
                if (!$this->isKoma && ($this->Properties["printKenkouSindanIppan"] != "1" || $this->Properties["KenkouSindan_Ippan_Pattern"] != "1")) {
                    $checkdata["L_EAR"]              = array(2,"聴力左状態コード", "F010");
                }
                if ($this->isShien) {
                    if (!$this->isFukui) {
                        $checkdata["L_EAR_DB_IN"]    = array(3,"(装用時)聴力左平均dB");
                        if ($this->Properties["useEar4000Hz"] == "1") {
                            $checkdata["L_EAR_DB_4000_IN"]   = array(3,"装用時)聴力左4000Hz");
                        }
                    }
                    $checkdata["L_EAR_IN"]           = array(2,"装用時)聴力左状態コード");
                }
                if ($Ippan == "0") {
                    $checkdata["ALBUMINURIA1CD"]     = array(2,"一次尿蛋白コード", "F020");
                    $checkdata["URICSUGAR1CD"]       = array(2,"一次尿糖コード", "F019");
                    $checkdata["URICBLEED1CD"]       = array(2,"一次尿潜血コード", "F018");
                    $checkdata["ALBUMINURIA2CD"]     = array(2,"二次尿蛋白コード", "F020");
                    $checkdata["URICSUGAR2CD"]       = array(2,"二次尿糖コード", "F019");
                    $checkdata["URICBLEED2CD"]       = array(2,"二次尿潜血コード", "F018");
                    $checkdata["URICOTHERTEST"]      = array(60,"尿その他の検査");
                    $checkdata["URI_ADVISECD"]       = array(2,"尿指導区分コード", "F021");
                }
                $checkdata["NUTRITIONCD"]            = array(2,"栄養状態コード", "F030");
                if ($Ippan == "1") {
                    $checkdata["NUTRITIONCD_REMARK"] = array(60,"栄養状態所見");
                }
                if ($Ippan != "0") {
                    $checkdata["SPINERIBCD"]         = array(2,"脊柱胸郭四肢コード", "F040");
                    $checkdata["SPINERIBCD_REMARK"]  = array(60,"脊柱胸郭四肢所見");
                }
                $checkdata["EYEDISEASECD"]           = array(2,"目疾病及異常コード", "F050");
                if ($Ippan == "2") {
                    $checkdata["EYEDISEASECD2"]      = array(2,"目疾病及異常コード2", "F050");
                    $checkdata["EYEDISEASECD3"]      = array(2,"目疾病及異常コード3", "F050");
                    $checkdata["EYEDISEASECD4"]      = array(2,"目疾病及異常コード4", "F050");
                }
                $checkdata["EYE_TEST_RESULT"]        = array(60,"眼科所見");
                if ($Ippan == "2") {
                    $checkdata["EYE_TEST_RESULT2"]   = array(60,"眼科所見2");
                    $checkdata["EYE_TEST_RESULT3"]   = array(60,"眼科所見3");
                }
                if ($Ippan == "1") {
                    if (!$this->isMigagi) { // 宮城は色覚異常を使用しない
                        $checkdata["EYEDISEASECD5"]  = array(2,"色覚異常コード", "F051");
                    }
                }
                if ($Ippan == "0") {
                    $checkdata["SPINERIBCD"]          = array(2,"脊柱胸郭四肢コード");
                    $checkdata["SPINERIBCD_REMARK"]   = array(60,"脊柱胸郭四肢所見");
                }
                $checkdata["NOSEDISEASECD"]           = array(2,"耳鼻咽頭疾患コード", "F060");
                if ($Ippan == "2") {
                    $checkdata["NOSEDISEASECD2"]      = array(2,"耳鼻咽頭疾患コード2", "F060");
                    $checkdata["NOSEDISEASECD3"]      = array(2,"耳鼻咽頭疾患コード3", "F060");
                    $checkdata["NOSEDISEASECD4"]      = array(2,"耳鼻咽頭疾患コード4", "F060");
                }
                $checkdata["NOSEDISEASECD_REMARK"]    = array(60,"耳鼻咽頭疾患所見");
                if ($Ippan == "2") {
                    $checkdata["NOSEDISEASECD_REMARK2"]  = array(60,"耳鼻咽頭疾患所見2");
                    $checkdata["NOSEDISEASECD_REMARK3"]  = array(60,"耳鼻咽頭疾患所見3");
                }
                if ($Ippan == "1") {
                    $checkdata["NOSEDISEASECD5"]         = array(2,"耳疾患コード", "F061");
                    $checkdata["NOSEDISEASECD_REMARK1"]  = array(60,"耳疾患所見");
                    $checkdata["NOSEDISEASECD6"]         = array(2,"鼻・副鼻腔疾患コード", "F062");
                    $checkdata["NOSEDISEASECD_REMARK2"]  = array(60,"鼻・副鼻腔疾患所見");
                    $checkdata["NOSEDISEASECD7"]         = array(2,"口腔咽頭疾患・異常コード", "F063");
                    $checkdata["NOSEDISEASECD_REMARK3"]  = array(60,"口腔咽頭疾患・異常所見");
                }
                $checkdata["SKINDISEASECD"]              = array(2,"皮膚疾患コード", "F070");
                if ($Ippan == "1" || $Ippan == "2") {
                    $checkdata["SKINDISEASECD_REMARK"]   = array(60,"皮膚疾患所見");
                }
                if ($this->Properties["printKenkouSindanIppan"] == "1" && $this->Properties["KenkouSindan_Ippan_Pattern"] == "1") {
                    $checkdata["OTHERDISEASECD"]           = array(2,"その他疾病及異常コード", "F140");
                    if ($Ippan != "2") {
                        if (!$this->isMie) {
                            $checkdata["OTHER_REMARK2"]    = array($this->maxOtherRemark2Byte,"その他疾病及異常所見");
                        }
                    }
                    if ($Ippan == "0" || $Ippan == "1") {
                        $checkdata["OTHER_ADVISECD"]       = array(2,"その他疾病指導区分コード", "F145");
                    }
                    if ($Ippan == "1" || $Ippan == "2") {
                        $checkdata["OTHER_REMARK"]         = array(60,"その他疾病指導区分所見");
                    }
                    if ($Ippan == "2") {
                        $checkdata["OTHER_REMARK2"]    = array($this->maxOtherRemark2Byte,"その他疾病及異常所見");
                        $checkdata["OTHER_REMARK3"]        = array(60,"その他疾病指導区分所見3");
                    }
                }
                if ($this->field["SCHOOL_KIND"] == "H") {
                    if ($Ippan != "0") {
                        $checkdata["TB_FILMDATE"]        = array(10,"結核撮影日付");
                        $checkdata["TB_FILMNO"]          = array($this->tb_filmnoFieldSize,"結核フィルム番号");
                        $checkdata["TB_REMARKCD"]        = array(2,"結核所見コード");
                        $checkdata["TB_X_RAY"]           = array(60,"結核所見");
                        $checkdata["TB_OTHERTESTCD"]     = array(2,"結核その他検査コード");
                    }
                    if ($Ippan == "2") {
                        $checkdata["TB_OTHERTEST_REMARK1"]  = array(60,"結核その他検査所見");
                    }
                }
                if ($Ippan == "1") {
                    $tb_namecd_label = ($this->isMigagi) ? "病名" : "疾病及び異常";
                } else {
                    $tb_namecd_label = "病名";
                }
                if ($Ippan != "0") {
                    $checkdata["TB_NAMECD"]           = array(2,"結核{$tb_namecd_label}コード");
                }
                if (!$this->isMie) {
                    $checkdata["TB_NAME_REMARK1"]     = array(60,"結核{$tb_namecd_label}所見");
                }
                if ($Ippan != "0") {
                    $checkdata["TB_ADVISECD"]         = array(2,"結核指導区分コード");
                }
                if ($Ippan == "2") {
                    $checkdata["TB_ADVISE_REMARK1"]   = array(60,"結核指導区分所見");
                }
                $checkdata["HEART_MEDEXAM"]           = array(2,"心臓臨床医学的検査コード", "F080");
                $checkdata["HEART_MEDEXAM_REMARK"]    = array(120,"心臓臨床医学的検査");
                if ($this->isKoma) {
                    $checkdata["HEART_GRAPH_NO"]      = array(12,"心電図No");
                }
                $checkdata["HEARTDISEASECD"]          = array(2,"心臓疾病及異常コード", "F090");
                $checkdata["HEARTDISEASECD_REMARK"]   = array(60,"心臓疾病及異常所見");
                if ($Ippan == "0" || $Ippan == "1") {
                    $checkdata["MANAGEMENT_DIV"]      = array(2,"心臓管理区分", "F091");
                }
                if ($Ippan == "1") {
                    $checkdata["MANAGEMENT_REMARK"]   = array(120,"心臓管理所見");
                }
                if ($Ippan == "0") {
                    if ($this->field["SCHOOL_KIND"] == "H") {
                        $checkdata["TB_FILMDATE"]      = array(10,"結核撮影日付");
                        $checkdata["TB_FILMNO"]        = array($this->tb_filmnoFieldSize,"結核フィルム番号");
                        $checkdata["TB_REMARKCD"]      = array(2,"結核所見コード", "F100");
                        $checkdata["TB_X_RAY"]         = array(60,"結核所見");
                        $checkdata["TB_OTHERTESTCD"]   = array(2,"結核その他検査コード", "F110");
                    }
                    $checkdata["TB_NAMECD"]            = array(2,"結核{$tb_namecd_label}コード", "F120");
                    $checkdata["TB_ADVISECD"]          = array(2,"結核指導区分コード", "F130");
                }
                if ($Ippan != "0") {
                    $checkdata["ALBUMINURIA1CD"]       = array(2,"一次尿蛋白コード", "F020");
                    $checkdata["URICSUGAR1CD"]         = array(2,"一次尿糖コード", "F019");
                    $checkdata["URICBLEED1CD"]         = array(2,"一次尿潜血コード", "F018");
                    if ($Ippan == 1 && $this->Properties["KenkouSindan_Ippan_Pattern"] == "1") {
                        $checkdata["URICPH1"]          = array(4,"二次尿ＰＨ");
                    }
                    $checkdata["ALBUMINURIA2CD"]       = array(2,"二次尿蛋白コード", "F020");
                    $checkdata["URICSUGAR2CD"]         = array(2,"二次尿糖コード", "F019");
                    $checkdata["URICBLEED2CD"]         = array(2,"二次尿潜血コード", "F018");
                    if ($Ippan == 1 && $this->Properties["KenkouSindan_Ippan_Pattern"] == "1") {
                        $checkdata["URICPH2"]          = array(4,"二次尿ＰＨ");
                    }
                    if ($Ippan == "2") {
                        $checkdata["URICOTHERTESTCD"]  = array(2,"尿その他の検査コード", "F022");
                    }
                    if ($Ippan != "2") {
                        $checkdata["URICOTHERTEST"]    = array(60,"尿その他の検査");
                    }
                    if ($Ippan == "1") {
                        $checkdata["URI_ADVISECD"]     = array(2,"尿指導区分コード");
                    }
                }
                if ($this->Properties["useParasite_".$this->field["SCHOOL_KIND"]] == "1") {
                    $checkdata["PARASITE"]             = array(2,"寄生虫卵", "F023");
                }
                if ($this->Properties["printKenkouSindanIppan"] != "1" || $this->Properties["KenkouSindan_Ippan_Pattern"] != "1") {
                    $checkdata["OTHERDISEASECD"]           = array(2,"その他疾病及異常コード", "F140");
                    if ($Ippan != "2") {
                        if (!$this->isMie) {
                            $checkdata["OTHER_REMARK2"]    = array($this->maxOtherRemark2Byte,"その他疾病及異常所見");
                        }
                    }
                    if ($Ippan == "0" || $Ippan == "1") {
                        $checkdata["OTHER_ADVISECD"]       = array(2,"その他疾病指導区分コード", "F145");
                    }
                    if ($Ippan == "1" || $Ippan == "2") {
                        $checkdata["OTHER_REMARK"]         = array(60,"その他疾病指導区分所見");
                    }
                    if ($Ippan == "2") {
                        $checkdata["OTHER_REMARK2"]        = array($this->maxOtherRemark2Byte,"その他疾病指導区分所見2");
                        $checkdata["OTHER_REMARK3"]        = array(60,"その他疾病指導区分所見3");
                    }
                }
                if ($this->isFukui) {// 福井は貧血
                    $checkdata["ANEMIA_REMARK"]        = array(30,"貧血所見");
                    $checkdata["HEMOGLOBIN"]           = array(4,"ヘモグロビン値");
                }
                $checkdata["DOC_DATE"]                 = array(10,"学校医日付");
                if ($Ippan == "0" || $Ippan == "1") {
                    if ($this->isMie) {
                        $checkdata["DOC_CD"]           = array(2,"学校医コード", "F144");
                    }
                    $checkdata["DOC_REMARK"]           = array(60,"学校医所見");
                }
                if ($Ippan != "0") {
                    $checkdata["TREATCD"]              = array(2,"事後処置コード", "F150");
                    if (!$this->isMie) {
                        $checkdata["TREATCD2"]         = array(2,"事後処置2コード", "F151");
                        $checkdata["TREAT_REMARK1"]    = array(60,"事後処置所見1");
                    }
                }
                if ($Ippan == "2") {
                    $checkdata["TREAT_REMARK2"]         = array(60,"事後処置所見2");
                    $checkdata["TREAT_REMARK3"]         = array(60,"事後処置所見3");
                }
                $checkdata["REMARK"]                    = array($remarkLen,"備考");
                if ($Ippan == "2") {
                    $checkdata["MESSAGE"]               = array(4,"メッセージ");
                }
                $checkdata["MEDICAL_HISTORY1"]          = array(2,"既往症1", "F143");
                $checkdata["MEDICAL_HISTORY2"]          = array(2,"既往症2", "F143");
                $checkdata["MEDICAL_HISTORY3"]          = array(2,"既往症3", "F143");
                $checkdata["DIAGNOSIS_NAME"]            = array(150,"診断名");
                $checkdata["GUIDE_DIV"]                 = array(1,"指導区分", "F141");
                $checkdata["JOINING_SPORTS_CLUB"]       = array(1,"運動部活動", "F142");
                $checkdata[$this->lastColumn]           = array(10,$this->lastColumn);
            } else {
                //歯・口腔
                $checkdata["YEAR"]                      = array(4,"※年度");
                $checkdata["TOOTH_DATE"]                = array(10,"※健診実施日");
                $checkdata["SCHREGNO"]                  = array(8,"※学籍番号");
                $checkdata["GRADE"]                     = array(2,"学年");
                $checkdata["HR_CLASS"]                  = array(3,"組");
                $checkdata["ATTENDNO"]                  = array(3,"出席番号");
                $checkdata["NAME"]                      = array("氏名");
                if (!($Ippan == "1" && $this->Properties["KenkouSindan_Ippan_Pattern"] == "1")) {
                    $checkdata["JAWS_JOINTCD2"]         = array(2,"顎関節コード", "F511");
                }
                if ($Ippan == "2"  || $Ippan == "3" || ($Ippan == "1" && ($this->z010name1 === "mieken" || $this->Properties["KenkouSindan_Ippan_Pattern"] == "1"))) {
                    if ($this->z010name1 === "kumamoto" && $this->field["SCHOOL_KIND"] == "H") {
                        if ($this->field["OUTPUT"] == "1") {
                            $checkdata["JAWS_JOINTCD"]  = array(2,"歯列・咬合コード", "F510");
                        } else {
                            $chkYear  = substr($this->field["YEAR"], 0, 4);
                            $chkGrade = substr($this->field["GRADE_HR_CLASS"], 0, 2);
                            if ((2015 < $chkYear && $chkYear < 2021) && $chkGrade == "") {
                                $checkdata["JAWS_JOINTCD"]    = array(2,"歯列コード", "F510");
                            } else {
                                $checkdata["JAWS_JOINTCD"]    = array(2,"歯列・咬合コード", "F510");
                            }
                            if (2016 > $chkYear) {
                                $checkdata["JAWS_JOINTCD"]    = array(2,"歯列コード", "F510");
                            }
                            $minGrade = knjf140aQuery::getMinGrade($this);
                            $setGrade = ($chkYear > 2022) ? 0: $chkYear - 2016 + $minGrade;
                            if ($setGrade > 0 && $chkGrade > sprintf("%02d", $setGrade)) {
                                $checkdata["JAWS_JOINTCD"]    = array(2,"歯列コード", "F510");
                            }
                        }
                    } else {
                        $checkdata["JAWS_JOINTCD"]            = array(2,"歯列コード", "F510");
                    }
                } else {
                    $checkdata["JAWS_JOINTCD"]            = array(2,"歯列・咬合コード", "F510");
                }
                if ($Ippan == "2"  || $Ippan == "3" || ($Ippan == "1" && ($this->z010name1 === "mieken" || $this->Properties["KenkouSindan_Ippan_Pattern"] == "1"))) {
                    if ($this->z010name1 === "kumamoto" && $this->field["SCHOOL_KIND"] == "H") {
                        if ($this->field["OUTPUT"] == "1") {
                        } else {
                            if ((2015 < $chkYear && $chkYear < 2021) && $chkGrade == "") {
                                $checkdata["JAWS_JOINTCD3"]   = array(2,"咬合コード", ($this->Properties["printKenkouSindanIppan"] == 1 && $this->Properties["KenkouSindan_Ippan_Pattern"] == "1" ? "F510" : "F512"));
                            }
                            if (2016 > $chkYear) {
                                $checkdata["JAWS_JOINTCD3"]   = array(2,"咬合コード", ($this->Properties["printKenkouSindanIppan"] == 1 && $this->Properties["KenkouSindan_Ippan_Pattern"] == "1" ? "F510" : "F512"));
                            }
                            if ($setGrade > 0 && $chkGrade > sprintf("%02d", $setGrade)) {
                                $checkdata["JAWS_JOINTCD3"]   = array(2,"咬合コード", ($this->Properties["printKenkouSindanIppan"] == 1 && $this->Properties["KenkouSindan_Ippan_Pattern"] == "1" ? "F510" : "F512"));
                            }
                        }
                    } else {
                        $checkdata["JAWS_JOINTCD3"]   = array(2,"咬合コード", ($this->Properties["printKenkouSindanIppan"] == 1 && $this->Properties["KenkouSindan_Ippan_Pattern"] == "1" ? "F510" : "F512"));
                    }
                }
                if ($Ippan == "1" && $this->Properties["KenkouSindan_Ippan_Pattern"] == "1") {
                    $checkdata["JAWS_JOINTCD2"]     = array(2,"顎関節コード", "F511");
                }
                $checkdata["PLAQUECD"]         = array(2,"歯垢".(($Ippan == "1" && $this->Properties["KenkouSindan_Ippan_Pattern"] == "1") ? "付着" : "状態")."コード", "F520");
                $checkdata["GUMCD"]            = array(2,"歯肉状態コード", "F513");
                if ($Ippan != "2") {
                    if (!$this->isMigagi && !($Ippan == "1" && $this->Properties["KenkouSindan_Ippan_Pattern"] == "1")) { // 宮城またはパターンプロパティ="1"(城東)は歯石沈着を使用しない
                        $checkdata["CALCULUS"] = array(2,"歯石沈着", "F521");
                    }
                }
                if (!$this->isMigagi) { // 宮城は矯正を使用しない
                    $checkdata["ORTHODONTICS"] = array(1,"矯正の有無");
                }
                $checkdata["UP_R_BABY5"]       = array(2,"乳歯 上 右E", "F550");
                $checkdata["UP_R_BABY4"]       = array(2,"乳歯 上 右D", "F550");
                $checkdata["UP_R_BABY3"]       = array(2,"乳歯 上 右C", "F550");
                $checkdata["UP_R_BABY2"]       = array(2,"乳歯 上 右B", "F550");
                $checkdata["UP_R_BABY1"]       = array(2,"乳歯 上 右A", "F550");
                $checkdata["UP_L_BABY1"]       = array(2,"乳歯 上 左A", "F550");
                $checkdata["UP_L_BABY2"]       = array(2,"乳歯 上 左B", "F550");
                $checkdata["UP_L_BABY3"]       = array(2,"乳歯 上 左C", "F550");
                $checkdata["UP_L_BABY4"]       = array(2,"乳歯 上 左D", "F550");
                $checkdata["UP_L_BABY5"]       = array(2,"乳歯 上 左E", "F550");
                $checkdata["LW_R_BABY5"]       = array(2,"乳歯 下 右E", "F550");
                $checkdata["LW_R_BABY4"]       = array(2,"乳歯 下 右D", "F550");
                $checkdata["LW_R_BABY3"]       = array(2,"乳歯 下 右C", "F550");
                $checkdata["LW_R_BABY2"]       = array(2,"乳歯 下 右B", "F550");
                $checkdata["LW_R_BABY1"]       = array(2,"乳歯 下 右A", "F550");
                $checkdata["LW_L_BABY1"]       = array(2,"乳歯 下 左A", "F550");
                $checkdata["LW_L_BABY2"]       = array(2,"乳歯 下 左B", "F550");
                $checkdata["LW_L_BABY3"]       = array(2,"乳歯 下 左C", "F550");
                $checkdata["LW_L_BABY4"]       = array(2,"乳歯 下 左D", "F550");
                $checkdata["LW_L_BABY5"]       = array(2,"乳歯 下 左E", "F550");
                $checkdata["BABYTOOTH"]        = array(2,"乳歯現在数");
                $checkdata["REMAINBABYTOOTH"]  = array(2,"乳歯未処置数");
                $checkdata["TREATEDBABYTOOTH"] = array(2,"乳歯処置数");
                $checkdata["BRACK_BABYTOOTH"]  = array(2,"要注意乳歯数");
                $checkdata["UP_R_ADULT8"]      = array(2,"永久歯 上 右8", "F550");
                $checkdata["UP_R_ADULT7"]      = array(2,"永久歯 上 右7", "F550");
                $checkdata["UP_R_ADULT6"]      = array(2,"永久歯 上 右6", "F550");
                $checkdata["UP_R_ADULT5"]      = array(2,"永久歯 上 右5", "F550");
                $checkdata["UP_R_ADULT4"]      = array(2,"永久歯 上 右4", "F550");
                $checkdata["UP_R_ADULT3"]      = array(2,"永久歯 上 右3", "F550");
                $checkdata["UP_R_ADULT2"]      = array(2,"永久歯 上 右2", "F550");
                $checkdata["UP_R_ADULT1"]      = array(2,"永久歯 上 右1", "F550");
                $checkdata["UP_L_ADULT1"]      = array(2,"永久歯 上 左1", "F550");
                $checkdata["UP_L_ADULT2"]      = array(2,"永久歯 上 左2", "F550");
                $checkdata["UP_L_ADULT3"]      = array(2,"永久歯 上 左3", "F550");
                $checkdata["UP_L_ADULT4"]      = array(2,"永久歯 上 左4", "F550");
                $checkdata["UP_L_ADULT5"]      = array(2,"永久歯 上 左5", "F550");
                $checkdata["UP_L_ADULT6"]      = array(2,"永久歯 上 左6", "F550");
                $checkdata["UP_L_ADULT7"]      = array(2,"永久歯 上 左7", "F550");
                $checkdata["UP_L_ADULT8"]      = array(2,"永久歯 上 左8", "F550");
                $checkdata["LW_R_ADULT8"]      = array(2,"永久歯 下 右8", "F550");
                $checkdata["LW_R_ADULT7"]      = array(2,"永久歯 下 右7", "F550");
                $checkdata["LW_R_ADULT6"]      = array(2,"永久歯 下 右6", "F550");
                $checkdata["LW_R_ADULT5"]      = array(2,"永久歯 下 右5", "F550");
                $checkdata["LW_R_ADULT4"]      = array(2,"永久歯 下 右4", "F550");
                $checkdata["LW_R_ADULT3"]      = array(2,"永久歯 下 右3", "F550");
                $checkdata["LW_R_ADULT2"]      = array(2,"永久歯 下 右2", "F550");
                $checkdata["LW_R_ADULT1"]      = array(2,"永久歯 下 右1", "F550");
                $checkdata["LW_L_ADULT1"]      = array(2,"永久歯 下 左1", "F550");
                $checkdata["LW_L_ADULT2"]      = array(2,"永久歯 下 左2", "F550");
                $checkdata["LW_L_ADULT3"]      = array(2,"永久歯 下 左3", "F550");
                $checkdata["LW_L_ADULT4"]      = array(2,"永久歯 下 左4", "F550");
                $checkdata["LW_L_ADULT5"]      = array(2,"永久歯 下 左5", "F550");
                $checkdata["LW_L_ADULT6"]      = array(2,"永久歯 下 左6", "F550");
                $checkdata["LW_L_ADULT7"]      = array(2,"永久歯 下 左7", "F550");
                $checkdata["LW_L_ADULT8"]      = array(2,"永久歯 下 左8", "F550");
                $checkdata["ADULTTOOTH"]       = array(2,"永久歯数");
                $checkdata["REMAINADULTTOOTH"] = array(2,"永久歯未処置数");
                $checkdata["TREATEDADULTTOOTH"] = array(2,"永久歯処置数");
                $checkdata["LOSTADULTTOOTH"]   = array(2,"永久歯喪失数");
                $checkdata["BRACK_ADULTTOOTH"] = array(2,"要観察歯数");
                if ($Ippan != "3") {
                    $checkdata["OTHERDISEASECD"]      = array(2,"その他疾病及び異常コード", "F530");
                }
                $checkdata["OTHERDISEASE"]            = array(60,"その他疾病及び異常");
                if ($this->isMigagi) {
                    $checkdata["OTHERDISEASECD3"]     = array(2,"その他疾病及び異常２コード", "F530");
                    $checkdata["OTHERDISEASECD4"]     = array(2,"その他疾病及び異常３コード", "F530");
                }
                if ($this->is_f020_otherdisese_hyouji2) {
                    $checkdata["OTHERDISEASECD3"]     = array(2,"その他疾病及び異常２コード", "F530");
                    $checkdata["OTHERDISEASE3"]       = array(60,"その他疾病及び異常２");
                    $checkdata["OTHERDISEASECD4"]     = array(2,"その他疾病及び異常３コード", "F530");
                    $checkdata["OTHERDISEASE4"]       = array(60,"その他疾病及び異常３");
                }
                if ($Ippan != "2") {
                    if (!$this->isMigagi) {
                        $checkdata["OTHERDISEASECD2"] = array(2,"口腔の疾病及び異常コード", "F531");
                    }
                    $checkdata["OTHERDISEASE2"]       = array(60,"口腔の疾病及び異常");
                    if ($this->is_f020_otherdisese_hyouji) {
                        $checkdata["OTHERDISEASE_REMARK1"] = array(2,"口腔の疾病及び異常コード2", "F531");
                        $checkdata["OTHERDISEASE_REMARK2"] = array(60,"口腔の疾病及び異常2");
                        $checkdata["OTHERDISEASE_REMARK3"] = array(2,"口腔の疾病及び異常コード3", "F531");
                        $checkdata["OTHERDISEASE_REMARK4"] = array(60,"口腔の疾病及び異常3");
                    }
                }
                if ($Ippan == "0" || $Ippan == "1") {
                    $checkdata["DENTISTREMARKCD"]           = array(2,"学校歯科医所見コード", "F540");
                    if (!$this->isMie) {
                        $checkdata["DENTISTREMARK"]         = array(60,"学校歯科医所見");
                    }
                    if ($this->is_f020_dentistremark_hyouji) {
                        $checkdata["DENTISTREMARK_REMARK1"] = array(2,"学校歯科医所見コード2", "F540");
                        $checkdata["DENTISTREMARK_REMARK2"] = array(60,"学校歯科医所見2");
                        $checkdata["DENTISTREMARK_REMARK3"] = array(2,"学校歯科医所見コード3", "F540");
                        $checkdata["DENTISTREMARK_REMARK4"] = array(60,"学校歯科医所見3");
                    }
                }
                if ($Ippan == "2" || $Ippan == "3") {
                    $checkdata["DENTISTREMARK_CO"]          = array(2,"学校歯科医所見 CO");
                    $checkdata["DENTISTREMARK_GO"]          = array(1,"学校歯科医所見 GO");
                    $checkdata["DENTISTREMARK_G"]           = array(1,"学校歯科医所見 G");
                }
                $checkdata["DENTISTREMARKDATE"]             = array(10,"学校歯科医所見日付");
                if ($Ippan != "3") {
                    $checkdata["DENTISTTREATCD"]            = array(2,"学校歯科医事後処置コード", "F541");
                }
                if (!$this->isMie) {
                    $setDentisttreatNum = ($Ippan == "2" || $Ippan == "3") ? 30: 60;
                    $checkdata["DENTISTTREAT"]              = array($setDentisttreatNum,"学校歯科医事後処置");
                }
                if ($Ippan == "2") {
                    $checkdata["DENTISTTREAT2"]             = array(30,"学校歯科医事後処置2");
                    $checkdata["DENTISTTREAT3"]             = array(30,"学校歯科医事後処置3");
                }
                $checkdata[$this->lastColumn]               = array(10,$this->lastColumn);
            }
            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            //エラーデータの削除
            $query = knjf140aQuery::deleteQueryErr();
            $result = $db->query($query);
            $i = 0;
            $record_no = 1;            //行番号(初期値)
            $primaryKey1 = array();
            $primaryKey2 = array();
            $primaryKey3 = array();
            $duplicateCsv1 = array();
            $duplicateCsv2 = array();
            $duplicateCsv3 = array();
            if ($this->field["CSVCD"] == "1") {
                $primaryKey1 = $this->getPrimaryKey('MEDEXAM_DET_DAT');
                $primaryKey2 = $this->getPrimaryKey('MEDEXAM_DET_DETAIL_DAT');
                $primaryKey3 = $this->getPrimaryKey('MEDEXAM_HDAT');
            } else {
                $primaryKey1 = $this->getPrimaryKey('MEDEXAM_TOOTH_DAT');
                $primaryKey2 = $this->getPrimaryKey('MEDEXAM_TOOTH_DETAIL_DAT');
                $primaryKey3 = $this->getPrimaryKey('MEDEXAM_HDAT');
            }
            $fp = fopen($this->field["FILE"]["tmp_name"], "r"); //ファイルまたはURLをオープンする
            while (!feof($fp)) {                                //ファイルポインタがファイル終端に達しているかどうか調べる
                $buffer = fgets($fp, 4096);                      //ファイルポインタから1行取得する
                if (trim($buffer) != '') {
                    //ヘッダ有なら１行目は無視
                    if ($this->field["HEADER"]=="on" && $record_no<2) {
                        $record_no++;
                        continue;
                    }
                    $buffer = i18n_convert($buffer, "UTF-8", "SJIS-win");        //文字列を指定コードに変換する

                    $tmp = explode(",", $buffer);
                    if ($this->field["CSVCD"] == "1") { //一般
                        $tmpCnt = 0;
                        $data["CSVCD"]                  = trim($tmp[$tmpCnt++]);
                        $data["YEAR"]                   = trim($tmp[$tmpCnt++]);
                        $data["DATE"]                   = str_replace("/", "-", $tmp[$tmpCnt++]);
                        $data["SCHREGNO"]               = trim($tmp[$tmpCnt++]);
                        $tmpCnt++;
                        $tmpCnt++;
                        $tmpCnt++;
                        $tmpCnt++;
                        $data["HEIGHT"]                 = trim($tmp[$tmpCnt++]);
                        $data["WEIGHT"]                 = trim($tmp[$tmpCnt++]);
                        if ($this->isFukui) {// 福井は座高使用しない
                            $data["NO_PRINT_OBESITY_INDEX"] = trim($tmp[$tmpCnt++]);
                        } else {
                            if (!$this->isMigagi && $this->Properties["hideSitHeight"] != "1") {
                                $data["SITHEIGHT"]          = trim($tmp[$tmpCnt++]);
                            }
                        }
                        $data["R_VISION_CANTMEASURE"]   = trim($tmp[$tmpCnt++]);
                        $data["L_VISION_CANTMEASURE"]   = trim($tmp[$tmpCnt++]);
                        if ($this->isHirokoudai) {
                            $data["R_BAREVISION"]           = trim($tmp[$tmpCnt++]);
                            $data["L_BAREVISION"]           = trim($tmp[$tmpCnt++]);
                            $data["R_VISION"]               = trim($tmp[$tmpCnt++]);
                            $data["L_VISION"]               = trim($tmp[$tmpCnt++]);
                        } else {
                            if ($Ippan == "0" || $this->isFukui) {
                                $data["R_BAREVISION"]       = trim($tmp[$tmpCnt++]);
                            }
                            $data["R_BAREVISION_MARK"]      = trim($tmp[$tmpCnt++]);
                            if ($Ippan == "0" || $this->isFukui) {
                                $data["L_BAREVISION"]       = trim($tmp[$tmpCnt++]);
                            }
                            $data["L_BAREVISION_MARK"]      = trim($tmp[$tmpCnt++]);
                            if ($Ippan == "0" || $this->isFukui) {
                                $data["R_VISION"]           = trim($tmp[$tmpCnt++]);
                            }
                            $data["R_VISION_MARK"]          = trim($tmp[$tmpCnt++]);
                            if ($Ippan == "0" || $this->isFukui) {
                                $data["L_VISION"]           = trim($tmp[$tmpCnt++]);
                            }
                            $data["L_VISION_MARK"]          = trim($tmp[$tmpCnt++]);
                        }
                        $data["R_EAR_CANTMEASURE"]      = trim($tmp[$tmpCnt++]);
                        if (!$this->isFukui && ($this->isShien || $this->Properties["unEar_db"] != "1")) {
                            if ($this->isKoma || ($this->Properties["printKenkouSindanIppan"] == "1" && $this->Properties["KenkouSindan_Ippan_Pattern"] == "1")) {
                                $data["R_EAR_DB_1000"]               = trim($tmp[$tmpCnt++]);
                            } else {
                                $data["R_EAR_DB"]               = trim($tmp[$tmpCnt++]);
                            }
                            if ($this->Properties["useEar4000Hz"] == "1") {
                                $data["R_EAR_DB_4000"]      = trim($tmp[$tmpCnt++]);
                            }
                        }
                        if (!$this->isKoma && ($this->Properties["printKenkouSindanIppan"] != "1" || $this->Properties["KenkouSindan_Ippan_Pattern"] != "1")) {
                            $data["R_EAR"]                  = trim($tmp[$tmpCnt++]);
                        }
                        if ($this->isShien) {
                            if (!$this->isFukui) {
                                $data["R_EAR_DB_IN"]          = trim($tmp[$tmpCnt++]);
                                if ($this->Properties["useEar4000Hz"] == "1") {
                                    $data["R_EAR_DB_4000_IN"] = trim($tmp[$tmpCnt++]);
                                }
                            }
                            $data["R_EAR_IN"]                 = trim($tmp[$tmpCnt++]);
                        }
                        $data["L_EAR_CANTMEASURE"]      = trim($tmp[$tmpCnt++]);
                        if (!$this->isFukui && ($this->isShien || $this->Properties["unEar_db"] != "1")) {
                            if ($this->isKoma || ($this->Properties["printKenkouSindanIppan"] == "1" && $this->Properties["KenkouSindan_Ippan_Pattern"] == "1")) {
                                $data["L_EAR_DB_1000"]               = trim($tmp[$tmpCnt++]);
                            } else {
                                $data["L_EAR_DB"]               = trim($tmp[$tmpCnt++]);
                            }
                            if ($this->Properties["useEar4000Hz"] == "1") {
                                $data["L_EAR_DB_4000"]      = trim($tmp[$tmpCnt++]);
                            }
                        }
                        if (!$this->isKoma && ($this->Properties["printKenkouSindanIppan"] != "1" || $this->Properties["KenkouSindan_Ippan_Pattern"] != "1")) {
                            $data["L_EAR"]                  = trim($tmp[$tmpCnt++]);
                        }
                        if ($this->isShien) {
                            if (!$this->isFukui) {
                                $data["L_EAR_DB_IN"]          = trim($tmp[$tmpCnt++]);
                                if ($this->Properties["useEar4000Hz"] == "1") {
                                    $data["L_EAR_DB_4000_IN"] = trim($tmp[$tmpCnt++]);
                                }
                            }
                            $data["L_EAR_IN"]                 = trim($tmp[$tmpCnt++]);
                        }
                        if ($Ippan == "0") {
                            $data["ALBUMINURIA1CD"]     = trim($tmp[$tmpCnt++]);
                            $data["URICSUGAR1CD"]       = trim($tmp[$tmpCnt++]);
                            $data["URICBLEED1CD"]       = trim($tmp[$tmpCnt++]);
                            $data["ALBUMINURIA2CD"]     = trim($tmp[$tmpCnt++]);
                            $data["URICSUGAR2CD"]       = trim($tmp[$tmpCnt++]);
                            $data["URICBLEED2CD"]       = trim($tmp[$tmpCnt++]);
                            $data["URICOTHERTEST"]      = trim($tmp[$tmpCnt++]);
                            $data["URI_ADVISECD"]       = trim($tmp[$tmpCnt++]);
                        }
                        $data["NUTRITIONCD"]            = trim($tmp[$tmpCnt++]);
                        if ($Ippan == "1") {
                            $data["NUTRITIONCD_REMARK"] = trim($tmp[$tmpCnt++]);
                        }
                        if ($Ippan != "0") {
                            $data["SPINERIBCD"]         = trim($tmp[$tmpCnt++]);
                            $data["SPINERIBCD_REMARK"]  = trim($tmp[$tmpCnt++]);
                        }
                        $data["EYEDISEASECD"]           = trim($tmp[$tmpCnt++]);
                        if ($Ippan == "2") {
                            $data["EYEDISEASECD2"]      = trim($tmp[$tmpCnt++]);
                            $data["EYEDISEASECD3"]      = trim($tmp[$tmpCnt++]);
                            $data["EYEDISEASECD4"]      = trim($tmp[$tmpCnt++]);
                        }
                        $data["EYE_TEST_RESULT"]        = trim($tmp[$tmpCnt++]);
                        if ($Ippan == "2") {
                            $data["EYE_TEST_RESULT2"]   = trim($tmp[$tmpCnt++]);
                            $data["EYE_TEST_RESULT3"]   = trim($tmp[$tmpCnt++]);
                        }
                        if ($Ippan == "1") {
                            if (!$this->isMigagi) { // 宮城は色覚異常を使用しない
                                $data["EYEDISEASECD5"]      = trim($tmp[$tmpCnt++]);
                            }
                        }
                        if ($Ippan == "0") {
                            $data["SPINERIBCD"]         = trim($tmp[$tmpCnt++]);
                            $data["SPINERIBCD_REMARK"]  = trim($tmp[$tmpCnt++]);
                        }
                        $data["NOSEDISEASECD"]          = trim($tmp[$tmpCnt++]);
                        if ($Ippan == "2") {
                            $data["NOSEDISEASECD2"]     = trim($tmp[$tmpCnt++]);
                            $data["NOSEDISEASECD3"]     = trim($tmp[$tmpCnt++]);
                            $data["NOSEDISEASECD4"]     = trim($tmp[$tmpCnt++]);
                        }
                        $data["NOSEDISEASECD_REMARK"]   = trim($tmp[$tmpCnt++]);
                        if ($Ippan == "2") {
                            $data["NOSEDISEASECD_REMARK2"]  = trim($tmp[$tmpCnt++]);
                            $data["NOSEDISEASECD_REMARK3"]  = trim($tmp[$tmpCnt++]);
                        }
                        if ($Ippan == "1") {
                            $data["NOSEDISEASECD5"]         = trim($tmp[$tmpCnt++]);
                            $data["NOSEDISEASECD_REMARK1"]  = trim($tmp[$tmpCnt++]);
                            $data["NOSEDISEASECD6"]         = trim($tmp[$tmpCnt++]);
                            $data["NOSEDISEASECD_REMARK2"]  = trim($tmp[$tmpCnt++]);
                            $data["NOSEDISEASECD7"]         = trim($tmp[$tmpCnt++]);
                            $data["NOSEDISEASECD_REMARK3"]  = trim($tmp[$tmpCnt++]);
                        }
                        $data["SKINDISEASECD"]          = trim($tmp[$tmpCnt++]);
                        if ($Ippan == "1" || $Ippan == "2") {
                            $data["SKINDISEASECD_REMARK"]   = trim($tmp[$tmpCnt++]);
                        }
                        if ($this->Properties["printKenkouSindanIppan"] == "1" && $this->Properties["KenkouSindan_Ippan_Pattern"] == "1") {
                            $data["OTHERDISEASECD"]         = trim($tmp[$tmpCnt++]);
                            if ($Ippan != "2") {
                                if (!$this->isMie) {
                                    $data["OTHER_REMARK2"]      = trim($tmp[$tmpCnt++]);
                                }
                            }
                            if ($Ippan == "0" || $Ippan == "1") {
                                $data["OTHER_ADVISECD"]     = trim($tmp[$tmpCnt++]);
                            }
                            if ($Ippan == "1" || $Ippan == "2") {
                                $data["OTHER_REMARK"]       = trim($tmp[$tmpCnt++]);
                            }
                            if ($Ippan == "2") {
                                $data["OTHER_REMARK2"]      = trim($tmp[$tmpCnt++]);
                                $data["OTHER_REMARK3"]      = trim($tmp[$tmpCnt++]);
                            }
                        }
                        if ($this->field["SCHOOL_KIND"] == "H") {
                            if ($Ippan != "0") {
                                $data["TB_FILMDATE"]        = str_replace("/", "-", $tmp[$tmpCnt++]);
                                $data["TB_FILMNO"]          = trim($tmp[$tmpCnt++]);
                                $data["TB_REMARKCD"]        = trim($tmp[$tmpCnt++]);
                                $data["TB_X_RAY"]           = trim($tmp[$tmpCnt++]);
                                $data["TB_OTHERTESTCD"]     = trim($tmp[$tmpCnt++]);
                            }
                            if ($Ippan == "2") {
                                $data["TB_OTHERTEST_REMARK1"]  = trim($tmp[$tmpCnt++]);
                            }
                        }
                        if ($Ippan != "0") {
                            $data["TB_NAMECD"]          = trim($tmp[$tmpCnt++]);
                        }
                        if (!$this->isMie) {
                            $data["TB_NAME_REMARK1"]    = trim($tmp[$tmpCnt++]);
                        }
                        if ($Ippan != "0") {
                            $data["TB_ADVISECD"]        = trim($tmp[$tmpCnt++]);
                        }
                        if ($Ippan == "2") {
                            $data["TB_ADVISE_REMARK1"]  = trim($tmp[$tmpCnt++]);
                        }
                        $data["HEART_MEDEXAM"]          = trim($tmp[$tmpCnt++]);
                        $data["HEART_MEDEXAM_REMARK"]   = trim($tmp[$tmpCnt++]);
                        if ($this->isKoma) {
                            $data["HEART_GRAPH_NO"]   = trim($tmp[$tmpCnt++]);
                        }
                        $data["HEARTDISEASECD"]         = trim($tmp[$tmpCnt++]);
                        $data["HEARTDISEASECD_REMARK"]  = trim($tmp[$tmpCnt++]);
                        if ($Ippan == "0" || $Ippan == "1") {
                            $data["MANAGEMENT_DIV"]     = trim($tmp[$tmpCnt++]);
                        }
                        if ($Ippan == "1") {
                            $data["MANAGEMENT_REMARK"]  = trim($tmp[$tmpCnt++]);
                        }
                        if ($Ippan == "0") {
                            if ($this->field["SCHOOL_KIND"] == "H") {
                                $data["TB_FILMDATE"]        = str_replace("/", "-", $tmp[$tmpCnt++]);
                                $data["TB_FILMNO"]          = trim($tmp[$tmpCnt++]);
                                $data["TB_REMARKCD"]        = trim($tmp[$tmpCnt++]);
                                $data["TB_X_RAY"]           = trim($tmp[$tmpCnt++]);
                                $data["TB_OTHERTESTCD"]     = trim($tmp[$tmpCnt++]);
                            }
                            $data["TB_NAMECD"]          = trim($tmp[$tmpCnt++]);
                            $data["TB_ADVISECD"]        = trim($tmp[$tmpCnt++]);
                        }
                        if ($Ippan != "0") {
                            $data["ALBUMINURIA1CD"]     = trim($tmp[$tmpCnt++]);
                            $data["URICSUGAR1CD"]       = trim($tmp[$tmpCnt++]);
                            $data["URICBLEED1CD"]       = trim($tmp[$tmpCnt++]);
                            if ($Ippan == 1 && $this->Properties["KenkouSindan_Ippan_Pattern"] == "1") {
                                $data["URICPH1"]       = trim($tmp[$tmpCnt++]);
                            }
                            $data["ALBUMINURIA2CD"]     = trim($tmp[$tmpCnt++]);
                            $data["URICSUGAR2CD"]       = trim($tmp[$tmpCnt++]);
                            $data["URICBLEED2CD"]       = trim($tmp[$tmpCnt++]);
                            if ($Ippan == 1 && $this->Properties["KenkouSindan_Ippan_Pattern"] == "1") {
                                $data["URICPH2"]       = trim($tmp[$tmpCnt++]);
                            }
                            if ($Ippan == "2") {
                                $data["URICOTHERTESTCD"]    = trim($tmp[$tmpCnt++]);
                            }
                            if ($Ippan != "2") {
                                $data["URICOTHERTEST"]  = trim($tmp[$tmpCnt++]);
                            }
                            if ($Ippan == "1") {
                                $data["URI_ADVISECD"]   = trim($tmp[$tmpCnt++]);
                            }
                        }
                        if ($this->Properties["useParasite_".$this->field["SCHOOL_KIND"]] == "1") {
                            $data["PARASITE"]           = trim($tmp[$tmpCnt++]);
                        }
                        if ($this->Properties["printKenkouSindanIppan"] != "1" || $this->Properties["KenkouSindan_Ippan_Pattern"] != "1") {
                            $data["OTHERDISEASECD"]         = trim($tmp[$tmpCnt++]);
                            if ($Ippan != "2") {
                                if (!$this->isMie) {
                                    $data["OTHER_REMARK2"]      = trim($tmp[$tmpCnt++]);
                                }
                            }
                            if ($Ippan == "0" || $Ippan == "1") {
                                $data["OTHER_ADVISECD"]     = trim($tmp[$tmpCnt++]);
                            }
                            if ($Ippan == "1" || $Ippan == "2") {
                                $data["OTHER_REMARK"]       = trim($tmp[$tmpCnt++]);
                            }
                            if ($Ippan == "2") {
                                $data["OTHER_REMARK2"]      = trim($tmp[$tmpCnt++]);
                                $data["OTHER_REMARK3"]      = trim($tmp[$tmpCnt++]);
                            }
                        }
                        if ($this->isFukui) { // 福井は貧血
                            $data["ANEMIA_REMARK"]      = trim($tmp[$tmpCnt++]);
                            $data["HEMOGLOBIN"]         = trim($tmp[$tmpCnt++]);
                        }
                        $data["DOC_DATE"]               = str_replace("/", "-", $tmp[$tmpCnt++]);
                        if ($Ippan == "0" || $Ippan == "1") {
                            if ($this->isMie) {
                                $data["DOC_CD"]             = trim($tmp[$tmpCnt++]);
                            }
                            $data["DOC_REMARK"]         = trim($tmp[$tmpCnt++]);
                        }
                        if ($Ippan != "0") {
                            $data["TREATCD"]            = trim($tmp[$tmpCnt++]);
                            if (!$this->isMie) {
                                $data["TREATCD2"]           = trim($tmp[$tmpCnt++]);
                                $data["TREAT_REMARK1"]      = trim($tmp[$tmpCnt++]);
                            }
                        }
                        if ($Ippan == "2") {
                            $data["TREAT_REMARK2"]      = trim($tmp[$tmpCnt++]);
                            $data["TREAT_REMARK3"]      = trim($tmp[$tmpCnt++]);
                        }
                        $data["REMARK"]                 = trim($tmp[$tmpCnt++]);
                        if ($Ippan == "2") {
                            $data["MESSAGE"]            = trim($tmp[$tmpCnt++]);
                        }
                        $data["MEDICAL_HISTORY1"]       = trim($tmp[$tmpCnt++]);
                        $data["MEDICAL_HISTORY2"]       = trim($tmp[$tmpCnt++]);
                        $data["MEDICAL_HISTORY3"]       = trim($tmp[$tmpCnt++]);
                        $data["DIAGNOSIS_NAME"]         = trim($tmp[$tmpCnt++]);
                        $data["GUIDE_DIV"]              = trim($tmp[$tmpCnt++]);
                        $data["JOINING_SPORTS_CLUB"]    = trim($tmp[$tmpCnt++]);
                    } else { //歯・口腔
                        $tmpCnt = 0;
                        $data["CSVCD"]                  = trim($tmp[$tmpCnt++]);
                        $data["YEAR"]                   = trim($tmp[$tmpCnt++]);
                        $data["TOOTH_DATE"]             = str_replace("/", "-", $tmp[$tmpCnt++]);
                        $data["SCHREGNO"]               = trim($tmp[$tmpCnt++]);
                        $tmpCnt++;
                        $tmpCnt++;
                        $tmpCnt++;
                        $tmpCnt++;
                        if (!($Ippan == "1" && $this->Properties["KenkouSindan_Ippan_Pattern"] == "1")) {
                            $data["JAWS_JOINTCD2"]          = trim($tmp[$tmpCnt++]);
                        }
                        $data["JAWS_JOINTCD"]           = trim($tmp[$tmpCnt++]);
                        if ($Ippan == "2"  || $Ippan == "3" || ($Ippan == "1" && ($this->z010name1 === "mieken" || $this->Properties["KenkouSindan_Ippan_Pattern"] == "1"))) {
                            if (!($this->z010name1 == "kumamoto" && $this->field["SCHOOL_KIND"] == "H")) {
                                $data["JAWS_JOINTCD3"]      = trim($tmp[$tmpCnt++]);
                            }
                        }
                        if ($Ippan == "1" && $this->Properties["KenkouSindan_Ippan_Pattern"] == "1") {
                            $data["JAWS_JOINTCD2"]          = trim($tmp[$tmpCnt++]);
                        }
                        $data["PLAQUECD"]               = trim($tmp[$tmpCnt++]);
                        $data["GUMCD"]                  = trim($tmp[$tmpCnt++]);
                        if ($Ippan != "2") {
                            if (!$this->isMigagi && !($Ippan == "1" && $this->Properties["KenkouSindan_Ippan_Pattern"] == "1")) { // 宮城またはパターンプロパティ="1"(城東)は歯石沈着を使用しない
                                $data["CALCULUS"]           = trim($tmp[$tmpCnt++]);
                            }
                        }
                        if (!$this->isMigagi) { // 宮城は矯正を使用しない
                            $data["ORTHODONTICS"]           = trim($tmp[$tmpCnt++]);
                        }
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
                        if ($Ippan != "3") {
                            $data["OTHERDISEASECD"]     = trim($tmp[$tmpCnt++]);
                        }
                        $data["OTHERDISEASE"]           = trim($tmp[$tmpCnt++]);
                        if ($this->isMigagi) {
                            $data["OTHERDISEASECD3"]    = trim($tmp[$tmpCnt++]);
                            $data["OTHERDISEASECD4"]    = trim($tmp[$tmpCnt++]);
                        }
                        if ($this->is_f020_otherdisese_hyouji2) {
                            $data["OTHERDISEASECD3"]    = trim($tmp[$tmpCnt++]);
                            $data["OTHERDISEASE3"]      = trim($tmp[$tmpCnt++]);
                            $data["OTHERDISEASECD4"]    = trim($tmp[$tmpCnt++]);
                            $data["OTHERDISEASE4"]      = trim($tmp[$tmpCnt++]);
                        }
                        if ($Ippan != "2") {
                            if (!$this->isMigagi) {
                                $data["OTHERDISEASECD2"]    = trim($tmp[$tmpCnt++]);
                            }
                            $data["OTHERDISEASE2"]      = trim($tmp[$tmpCnt++]);
                            if ($this->is_f020_otherdisese_hyouji) {
                                $data["OTHERDISEASE_REMARK1"]    = trim($tmp[$tmpCnt++]);
                                $data["OTHERDISEASE_REMARK2"]    = trim($tmp[$tmpCnt++]);
                                $data["OTHERDISEASE_REMARK3"]    = trim($tmp[$tmpCnt++]);
                                $data["OTHERDISEASE_REMARK4"]    = trim($tmp[$tmpCnt++]);
                            }
                        }
                        if ($Ippan == "0" || $Ippan == "1") {
                            $data["DENTISTREMARKCD"]    = trim($tmp[$tmpCnt++]);
                            if (!$this->isMie) {
                                $data["DENTISTREMARK"]      = trim($tmp[$tmpCnt++]);
                            }
                            if ($this->is_f020_dentistremark_hyouji) {
                                $data["DENTISTREMARK_REMARK1"]    = trim($tmp[$tmpCnt++]);
                                $data["DENTISTREMARK_REMARK2"]    = trim($tmp[$tmpCnt++]);
                                $data["DENTISTREMARK_REMARK3"]    = trim($tmp[$tmpCnt++]);
                                $data["DENTISTREMARK_REMARK4"]    = trim($tmp[$tmpCnt++]);
                            }
                        }
                        if ($Ippan == "2" || $Ippan == "3") {
                            $data["DENTISTREMARK_CO"]   = trim($tmp[$tmpCnt++]);
                            $data["DENTISTREMARK_GO"]   = trim($tmp[$tmpCnt++]);
                            $data["DENTISTREMARK_G"]    = trim($tmp[$tmpCnt++]);
                        }
                        $data["DENTISTREMARKDATE"]      = str_replace("/", "-", $tmp[$tmpCnt++]);
                        if ($Ippan != "3") {
                            $data["DENTISTTREATCD"]     = trim($tmp[$tmpCnt++]);
                        }
                        if (!$this->isMie) {
                            $data["DENTISTTREAT"]       = trim($tmp[$tmpCnt++]);
                        }
                        if ($Ippan == "2") {
                            $data["DENTISTTREAT2"]      = trim($tmp[$tmpCnt++]);
                            $data["DENTISTTREAT3"]      = trim($tmp[$tmpCnt++]);
                        }

                        $data["ORTHODONTICS"] = $data["ORTHODONTICS"] ? $data["ORTHODONTICS"] : 0; //矯正の有無(初期値を0とする)
                        if ($Ippan == "2" || $Ippan == "3") {
                            $data["DENTISTREMARK_GO"] = $data["DENTISTREMARK_GO"] ? $data["DENTISTREMARK_GO"] : 0; //学校歯科医所見GOの有無(初期値を0とする)
                            $data["DENTISTREMARK_G"]  = $data["DENTISTREMARK_G"] ? $data["DENTISTREMARK_G"] : 0; //学校歯科医所見Gの有無(初期値を0とする)
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
        $existenceErrFlg    = false;    //存在エラーフラグ
        $duplicateCsvErrFlg = false;    //重複(CSV)エラーフラグ

        $hissuErrMsg        = "";       //必須エラーメッセージ
        $numericErrMsg      = "";       //数値エラーメッセージ
        $lengthErrMsg       = "";       //レングスエラーメッセージ
        $masterErrMsg       = "";       //存在エラーメッセージ
        $valuesErrMsg       = "";       //値エラーメッセージ
        $existenceErrMsg    = "";       //存在エラーメッセージ
        $duplicateCsvErrMsg = "";       //重複(CSV)メッセージ

        $hissuSep        = "";
        $numericSep      = "";
        $lengthSep       = "";
        $masterSep       = "";
        $valuesSep       = "";
        $existenceSep    = "";
        $duplicateCsvSep = "";

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
                        in_array($key, array("YEAR", "SCHREGNO", "R_EAR_DB", "R_EAR_DB_1000", "R_EAR_DB_4000", "R_EAR", "R_EAR_DB_IN", "R_EAR_DB_4000_IN", "R_EAR_IN"
                        , "L_EAR_DB", "L_EAR_DB_1000", "L_EAR_DB_4000", "L_EAR", "L_EAR_DB_IN", "L_EAR_DB_4000_IN", "L_EAR_IN", "ALBUMINURIA1CD", "URICSUGAR1CD"
                        , "URICBLEED1CD", "ALBUMINURIA2CD", "URICSUGAR2CD", "URICBLEED2CD", "URICOTHERTESTCD", "URI_ADVISECD", "NUTRITIONCD", "SPINERIBCD", "EYEDISEASECD"
                        , "EYEDISEASECD2", "EYEDISEASECD3", "EYEDISEASECD4", "EYEDISEASECD5", "NOSEDISEASECD", "NOSEDISEASECD2", "NOSEDISEASECD3", "NOSEDISEASECD4"
                        , "NOSEDISEASECD5", "NOSEDISEASECD6", "NOSEDISEASECD7", "SKINDISEASECD", "TB_REMARKCD", "TB_OTHERTESTCD", "TB_NAMECD", "TB_ADVISECD", "HEART_MEDEXAM"
                        , "HEARTDISEASECD", "MANAGEMENT_DIV", "PARASITE", "OTHERDISEASECD", "OTHER_ADVISECD", "TREATCD", "TREATCD2", "GUIDE_DIV", "JOINING_SPORTS_CLUB"
                        , "MEDICAL_HISTORY1", "MEDICAL_HISTORY2", "MEDICAL_HISTORY3")) && $data[$key] != "" && !is_numeric($data[$key])
                    ) {
                        $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                    }
                    if (
                        in_array($key, array("HEIGHT", "WEIGHT", "SITHEIGHT", "R_BAREVISION", "L_BAREVISION", "R_VISION", "L_VISION", "HEMOGLOBIN")) && $data[$key] != ""
                        && !is_numeric(str_replace(".", "", $data[$key]))
                    ) {
                        $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                    }
                    if ($this->Properties["printKenkouSindanIppan"] == 1 && $this->Properties["KenkouSindan_Ippan_Pattern"] == "1") {
                        if (in_array($key, array("URICPH1", "URICPH2"))) {
                            if ($data[$key] != "" && !is_numeric($data[$key])) {
                                $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                            }
                        }
                    }
                    if ($this->isMie && $key == "DOC_CD" && $data["DOC_CD"] != "" && !is_numeric($data["DOC_CD"])) {
                        $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                    }
                    //値チェック
                    if (in_array($key, array("HEIGHT", "WEIGHT", "SITHEIGHT"))) {
                        if ($data[$key] != "" && !$this->ataiCheck($data[$key], 3, 1)) {
                            $returnFlg = $this->errSet($valuesErrFlg, $valuesErrMsg, "※値不正:", $valuesSep, $val[1]);
                        }
                    }
                    if (in_array($key, array("R_BAREVISION", "L_BAREVISION", "R_VISION", "L_VISION"))) {
                        if ($data[$key] != "" && !$this->ataiCheck($data[$key], 1, 3)) {
                            $returnFlg = $this->errSet($valuesErrFlg, $valuesErrMsg, "※値不正:", $valuesSep, $val[1]);
                        }
                    }
                    if ($key == "HEMOGLOBIN" && $data[$key] != "" && !$this->ataiCheck($data["HEMOGLOBIN"], 2, 1)) {
                        $returnFlg = $this->errSet($valuesErrFlg, $valuesErrMsg, "※値不正:", $valuesSep, $val[1]);
                    }

                    //文字数チェック
                    if (
                        in_array($key, array("YEAR", "DATE", "SCHREGNO", "HEIGHT", "WEIGHT", "SITHEIGHT", "NO_PRINT_OBESITY_INDEX", "R_VISION_CANTMEASURE"
                        , "L_VISION_CANTMEASURE", "R_BAREVISION", "R_BAREVISION_MARK", "L_BAREVISION", "L_BAREVISION_MARK", "R_VISION", "R_VISION_MARK", "L_VISION"
                        , "L_VISION_MARK", "R_EAR_CANTMEASURE", "R_EAR_DB", "R_EAR_DB_1000", "R_EAR_DB_4000", "R_EAR", "R_EAR_DB_IN", "R_EAR_DB_4000_IN", "R_EAR_IN"
                        , "L_EAR_CANTMEASURE", "L_EAR_DB", "L_EAR_DB_1000", "L_EAR_DB_4000", "L_EAR", "L_EAR_DB_IN", "L_EAR_DB_4000_IN", "L_EAR_IN", "ALBUMINURIA1CD"
                        , "URICSUGAR1CD", "URICBLEED1CD", "ALBUMINURIA2CD", "URICSUGAR2CD", "URICBLEED2CD", "URICOTHERTESTCD", "URICOTHERTEST", "URI_ADVISECD"
                        , "NUTRITIONCD", "NUTRITIONCD_REMARK", "SPINERIBCD", "SPINERIBCD_REMARK", "EYEDISEASECD", "EYEDISEASECD2", "EYEDISEASECD3", "EYEDISEASECD4"
                        , "EYE_TEST_RESULT", "EYE_TEST_RESULT2", "EYE_TEST_RESULT3", "EYEDISEASECD5", "NOSEDISEASECD", "NOSEDISEASECD2", "NOSEDISEASECD3", "NOSEDISEASECD4"
                        , "NOSEDISEASECD5", "NOSEDISEASECD6", "NOSEDISEASECD7", "NOSEDISEASECD_REMARK", "NOSEDISEASECD_REMARK1", "NOSEDISEASECD_REMARK2", "NOSEDISEASECD_REMARK3"
                        , "SKINDISEASECD", "SKINDISEASECD_REMARK", "TB_FILMDATE", "TB_FILMNO", "TB_REMARKCD", "TB_X_RAY", "TB_OTHERTESTCD", "TB_OTHERTEST_REMARK1", "TB_NAMECD"
                        , "TB_NAME_REMARK1", "TB_ADVISECD", "TB_ADVISE_REMARK1", "HEART_MEDEXAM", "HEART_MEDEXAM_REMARK", "HEART_GRAPH_NO", "HEARTDISEASECD", "HEARTDISEASECD_REMARK"
                        , "MANAGEMENT_DIV", "MANAGEMENT_REMARK", "PARASITE", "OTHERDISEASECD", "OTHER_ADVISECD", "OTHER_REMARK", "OTHER_REMARK2", "OTHER_REMARK3", "ANEMIA_REMARK"
                        , "HEMOGLOBIN", "DOC_DATE", "DOC_REMARK", "TREATCD", "TREATCD2", "TREAT_REMARK1", "TREAT_REMARK2", "TREAT_REMARK3", "REMARK", "MEDICAL_HISTORY1"
                        , "MEDICAL_HISTORY2", "MEDICAL_HISTORY3", "DIAGNOSIS_NAME", "GUIDE_DIV", "JOINING_SPORTS_CLUB")) && strlen($data[$key]) > $val[0]
                    ) {
                        $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                    }
                    if (in_array($key, array("DOC_CD"))) {
                        if ($this->isMie && strlen($data[$key]) > $val[0]) {
                            $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                        }
                    }
                    if ($this->Properties["printKenkouSindanIppan"] == 1 && $this->Properties["KenkouSindan_Ippan_Pattern"] == "1") {
                        if (in_array($key, array("URICPH1","URICPH2"))) {
                            if (strlen($data[$key]) > $val[0]) {
                                $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                            }
                        }
                    }
                    if ($this->Properties["printKenkouSindanIppan"] == "2" && $this->validate_row_cnt($data["MESSAGE"], (21 * 2)) > 4) {
                        $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                    }

                    //存在チェック（名称マスタ）
                    if ($this->Properties["printKenkouSindanIppan"] == "2") {
                        if ($key == "EYEDISEASECD" && strlen($data[$key]) == 0) {
                            //目の疾病及び異常の初期値
                            $chkEyedisease_Shokichi = $db->getOne(knjf140aQuery::getNameMstDef($this, "F050", "1"));
                            if (strlen($chkEyedisease_Shokichi) == 0) {
                                $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, $val[1]."( 目の疾病及び異常 )");
                            }
                        }
                        if ($key == "NOSEDISEASECD" && strlen($data[$key]) == 0) {
                            //耳鼻咽頭疾患の初期値
                            $chkNosedisease_Shokichi = $db->getOne(knjf140aQuery::getNameMstDef($this, "F060", "1"));
                            if (strlen($chkNosedisease_Shokichi) == 0) {
                                $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, $val[1]."( 耳鼻咽頭疾患 )");
                            }
                        }
                    }
                    //視力の文字チェック
                    if ($this->isHirokoudai) {
                        $siryokumoji = "視力・名称マスタの設定(F017)";
                    } else {
                        $siryokumoji = "未対応の文字(視力)";
                    }
                    if (in_array($key, array("R_BAREVISION_MARK"))) {
                        $err = $this->siryokumojiCheck($data["R_BAREVISION_MARK"], $data["R_BAREVISION"], $db, 'R_BAREVISION');
                        if ($err) {
                            $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $siryokumoji);
                        }
                    }
                    if (in_array($key, array("L_BAREVISION_MARK"))) {
                        $err = $this->siryokumojiCheck($data["L_BAREVISION_MARK"], $data["L_BAREVISION"], $db, 'L_BAREVISION');
                        if ($err) {
                            $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $siryokumoji);
                        }
                    }
                    if (in_array($key, array("R_VISION_MARK"))) {
                        $err = $this->siryokumojiCheck($data["R_VISION_MARK"], $data["R_VISION"], $db, 'R_VISION');
                        if ($err) {
                            $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $siryokumoji);
                        }
                    }
                    if (in_array($key, array("L_VISION_MARK"))) {
                        $err = $this->siryokumojiCheck($data["L_VISION_MARK"], $data["L_VISION"], $db, 'L_VISION');
                        if ($err) {
                            $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $siryokumoji);
                        }
                    }
                    //歯・口腔-----
                } else {
                    //必須項目チェック
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
                    //熊本仕様
                    if ($this->Properties["printKenkouSindanIppan"] == "2" || $this->Properties["printKenkouSindanIppan"] == "3") {
                        if (in_array($key, array("DENTISTREMARK_GO", "DENTISTREMARK_G"))) {
                            if (!in_array($data[$key], array("0","1"))) {
                                $this->errSet($valuesErrFlg, $valuesErrMsg, "※値不正:", $valuesSep, $val[1]);
                            }
                        }
                    }
                    //数値チェック
                    if (
                        in_array($key, array("YEAR", "SCHREGNO", "JAWS_JOINTCD", "JAWS_JOINTCD3", "JAWS_JOINTCD2", "PLAQUECD", "GUMCD", "CALCULUS", "ORTHODONTICS"
                        , "UP_R_BABY5", "UP_R_BABY4", "UP_R_BABY3", "UP_R_BABY2", "UP_R_BABY1", "UP_L_BABY1", "UP_L_BABY2", "UP_L_BABY3", "UP_L_BABY4", "UP_L_BABY5"
                        , "LW_R_BABY5", "LW_R_BABY4", "LW_R_BABY3", "LW_R_BABY2", "LW_R_BABY1", "LW_L_BABY1", "LW_L_BABY2", "LW_L_BABY3", "LW_L_BABY4", "LW_L_BABY5"
                        , "BABYTOOTH", "REMAINBABYTOOTH", "TREATEDBABYTOOTH", "BRACK_BABYTOOTH", "UP_R_ADULT8", "UP_R_ADULT7", "UP_R_ADULT6", "UP_R_ADULT5", "UP_R_ADULT4"
                        , "UP_R_ADULT3", "UP_R_ADULT2", "UP_R_ADULT1", "UP_L_ADULT1", "UP_L_ADULT2", "UP_L_ADULT3", "UP_L_ADULT4", "UP_L_ADULT5", "UP_L_ADULT6", "UP_L_ADULT7"
                        , "UP_L_ADULT8", "LW_R_ADULT8", "LW_R_ADULT7", "LW_R_ADULT6", "LW_R_ADULT5", "LW_R_ADULT4", "LW_R_ADULT3", "LW_R_ADULT2", "LW_R_ADULT1", "LW_L_ADULT1"
                        , "LW_L_ADULT2", "LW_L_ADULT3", "LW_L_ADULT4", "LW_L_ADULT5", "LW_L_ADULT6", "LW_L_ADULT7", "LW_L_ADULT8", "ADULTTOOTH", "REMAINADULTTOOTH", "TREATEDADULTTOOTH"
                        , "LOSTADULTTOOTH", "BRACK_ADULTTOOTH", "OTHERDISEASECD", "OTHERDISEASECD3", "OTHERDISEASECD4", "OTHERDISEASECD2", "DENTISTREMARK_CO", "DENTISTREMARKCD"
                        , "DENTISTTREATCD"))
                    ) {
                        if ($data[$key] != "" && !is_numeric($data[$key])) {
                            $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                        }
                    }
                    if ($this->is_f020_otherdisese_hyouji) {
                        if (in_array($key, array("OTHERDISEASE_REMARK1", "OTHERDISEASE_REMARK3"))) {
                            if ($data[$key] != "" && !is_numeric($data[$key])) {
                                $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                            }
                        }
                    }
                    if ($this->is_f020_dentistremark_hyouji) {
                        if (in_array($key, array("DENTISTREMARK_REMARK1", "DENTISTREMARK_REMARK3"))) {
                            if ($data[$key] != "" && !is_numeric($data[$key])) {
                                $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                            }
                        }
                    }
                    //文字数チェック
                    if (
                        in_array($key, array("YEAR", "TOOTH_DATE", "SCHREGNO", "JAWS_JOINTCD", "JAWS_JOINTCD3", "JAWS_JOINTCD2", "PLAQUECD", "GUMCD", "CALCULUS", "ORTHODONTICS"
                        , "UP_R_BABY5", "UP_R_BABY4", "UP_R_BABY3", "UP_R_BABY2", "UP_R_BABY1", "UP_L_BABY1", "UP_L_BABY2", "UP_L_BABY3", "UP_L_BABY4", "UP_L_BABY5", "LW_R_BABY5"
                        , "LW_R_BABY4", "LW_R_BABY3", "LW_R_BABY2", "LW_R_BABY1", "LW_L_BABY1", "LW_L_BABY2", "LW_L_BABY3", "LW_L_BABY4", "LW_L_BABY5","BABYTOOTH","REMAINBABYTOOTH"
                        , "TREATEDBABYTOOTH", "BRACK_BABYTOOTH", "UP_R_ADULT8", "UP_R_ADULT7", "UP_R_ADULT6", "UP_R_ADULT5", "UP_R_ADULT4", "UP_R_ADULT3", "UP_R_ADULT2"
                        , "UP_R_ADULT1", "UP_L_ADULT1", "UP_L_ADULT2", "UP_L_ADULT3", "UP_L_ADULT4", "UP_L_ADULT5", "UP_L_ADULT6", "UP_L_ADULT7", "UP_L_ADULT8", "LW_R_ADULT8"
                        , "LW_R_ADULT7", "LW_R_ADULT6", "LW_R_ADULT5", "LW_R_ADULT4", "LW_R_ADULT3", "LW_R_ADULT2", "LW_R_ADULT1", "LW_L_ADULT1", "LW_L_ADULT2", "LW_L_ADULT3", "LW_L_ADULT4"
                        , "LW_L_ADULT5", "LW_L_ADULT6", "LW_L_ADULT7", "LW_L_ADULT8", "ADULTTOOTH", "REMAINADULTTOOTH", "TREATEDADULTTOOTH", "LOSTADULTTOOTH", "BRACK_ADULTTOOTH", "OTHERDISEASECD"
                        , "OTHERDISEASE", "OTHERDISEASECD3", "OTHERDISEASECD4", "OTHERDISEASECD2", "OTHERDISEASE2", "DENTISTREMARKCD", "DENTISTREMARK", "DENTISTREMARK_CO", "DENTISTREMARK_GO"
                        , "DENTISTREMARK_G", "DENTISTREMARKDATE", "DENTISTTREATCD", "DENTISTTREAT2", "DENTISTTREAT3", "DENTISTTREAT")) && strlen($data[$key]) > $val[0]
                    ) {
                        $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                    }
                    if ($this->is_f020_otherdisese_hyouji2) {
                        if (in_array($key, array("OTHERDISEASE3", "OTHERDISEASE4"))) {
                            if (strlen($data[$key]) > $val[0]) {
                                $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                            }
                        }
                    }
                    if ($this->is_f020_otherdisese_hyouji) {
                        if (in_array($key, array("OTHERDISEASE_REMARK1", "OTHERDISEASE_REMARK2", "OTHERDISEASE_REMARK3", "OTHERDISEASE_REMARK4"))) {
                            if (strlen($data[$key]) > $val[0]) {
                                $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                            }
                        }
                    }
                    if ($this->is_f020_dentistremark_hyouji) {
                        if (in_array($key, array("DENTISTREMARK_REMARK1", "DENTISTREMARK_REMARK2", "DENTISTREMARK_REMARK3", "DENTISTREMARK_REMARK4"))) {
                            if (strlen($data[$key]) > $val[0]) {
                                $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                            }
                        }
                    }
                }   //csvcd
            //削除
            } else {
                //一般-----
                //歯・口腔-----
                //桁数チェック
                //必須項目チェック
                if (in_array($key, array("YEAR", "SCHREGNO"))) {
                    if (!strlen(trim($data[$key]))) {
                        $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                    }
                }
                if ($key == "DATE" && $this->field["CSVCD"] == "1" && !strlen(trim($data["DATE"]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                }
                if ($key == "TOOTH_DATE" && $this->field["CSVCD"] == "2" && !strlen(trim($data["TOOTH_DATE"]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                }
                //数値チェック
                if (in_array($key, array("YEAR", "SCHREGNO"))) {
                    if ($data[$key] != "" && !is_numeric($data[$key])) {
                        $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                    }
                }
                //文字数チェック
                if (in_array($key, array("YEAR", "DATE", "TOOTH_DATE", "SCHREGNO")) && strlen($data[$key]) > $val[0]) {
                    $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                }
            }
        }

        if ($returnFlg) {
            foreach ($checkdata as $key => $val) {
                if ($this->field["CSVCD"] == "1") {
                    //日付の書式チェック
                    if (in_array($key, array("DATE","TB_FILMDATE","DOC_DATE"))) {
                        if ($data[$key] != "" && !$this->dateRep($data[$key])) {
                            $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※日付:", $numericSep, $val[1]);
                        }
                    }
                    //歯・口腔-----
                } else {
                    //日付の書式チェック
                    if (in_array($key, array("TOOTH_DATE","DENTISTREMARKDATE"))) {
                        if ($data[$key] != "" && !$this->dateRep($data[$key])) {
                            $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※日付:", $numericSep, $val[1]);
                        }
                    }
                }

                if ($data[$key] != "" && $val[2] != "") {
                    $query = knjf140aQuery::getMasterCheck($this, $data, "1", $val[2], $data[$key]);
                    $mark = $db->getOne($query);
                    if ($mark < 1) {
                        $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, $val[1]."(".$val[2].")");
                    }
                }
            }
            //存在チェック(更新時のみ)
            if ($data["YEAR"] != "" && $data["SCHREGNO"] != "") {
                $query = knjf140aQuery::getMasterCheck($this, $data, "2");
                if ($db->getOne($query) == 0) {
                    $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, "在籍データ");
                }
            }
        }
        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$hissuErrMsg.$numericErrMsg.$valuesErrMsg.$masterErrMsg.$lengthErrMsg.$existenceErrMsg.$otherErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knjf140aQuery::insertQueryErr($db, $line, $checkError);
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
            $header["CSVCD"]                        = "※CSVCD";
            $header["YEAR"]                         = "※年度";
            $header["DATE"]                         = "※健診実施日";
            $header["SCHREGNO"]                     = "※学籍番号";
            $header["GRADE"]                        = "学年";
            $header["HR_CLASS"]                     = "組";
            $header["ATTENDNO"]                     = "出席番号";
            $header["NAME"]                         = "氏名";
            $header["HEIGHT"]                       = "身長";
            $header["WEIGHT"]                       = "体重";
            if ($this->isFukui) {// 福井は座高使用しない
                $header["NO_PRINT_OBESITY_INDEX"]   = "帳票印字無し（肥満度）";
            } else {
                if (!$this->isMigagi && $this->Properties["hideSitHeight"] != "1") {
                    $header["SITHEIGHT"]                = "座高";
                }
            }
            $header["R_VISION_CANTMEASURE"]         = "視力(右)測定困難";
            $header["L_VISION_CANTMEASURE"]         = "視力(左)測定困難";
            if ($this->isHirokoudai) {
                $header["R_BAREVISION"]                 = "視力右裸眼(数字)";
                $header["L_BAREVISION"]                 = "視力左裸眼(数字)";
                $header["R_VISION"]                     = "視力右矯正(数字)";
                $header["L_VISION"]                     = "視力左矯正(数字)";
            } else {
                if ($Ippan == "0" || $this->isFukui) {
                    $header["R_BAREVISION"]             = "視力右裸眼(数字)";
                }
                $header["R_BAREVISION_MARK"]            = "視力右裸眼(文字)";
                if ($Ippan == "0" || $this->isFukui) {
                    $header["L_BAREVISION"]             = "視力左裸眼(数字)";
                }
                $header["L_BAREVISION_MARK"]            = "視力左裸眼(文字)";
                if ($Ippan == "0" || $this->isFukui) {
                    $header["R_VISION"]                 = "視力右矯正(数字)";
                }
                $header["R_VISION_MARK"]                = "視力右矯正(文字)";
                if ($Ippan == "0" || $this->isFukui) {
                    $header["L_VISION"]                 = "視力左矯正(数字)";
                }
                $header["L_VISION_MARK"]                = "視力左矯正(文字)";
            }
            $setEarTitle = "平均dB";
            //広工大は文言変更
            if ($this->isHirokoudai) {
                $setEarTitle = "1000Hz";
            }
            $header["R_EAR_CANTMEASURE"]            = "聴力(右)測定困難";
            if (!$this->isFukui && ($this->isShien || $this->Properties["unEar_db"] != "1")) {
                if ($this->isKoma || ($this->Properties["printKenkouSindanIppan"] == "1" && $this->Properties["KenkouSindan_Ippan_Pattern"] == "1")) {
                    $header["R_EAR_DB_1000"]                = "聴力右1000Hz";
                } else {
                    $header["R_EAR_DB"]                     = "聴力右".$setEarTitle;
                }
                if ($this->Properties["useEar4000Hz"] == "1") {
                    $header["R_EAR_DB_4000"]            = "聴力右4000Hz";
                }
            }
            if (!$this->isKoma && ($this->Properties["printKenkouSindanIppan"] != "1" || $this->Properties["KenkouSindan_Ippan_Pattern"] != "1")) {
                $header["R_EAR"]                        = "聴力右状態コード";
            }
            if ($this->isShien) {
                if (!$this->isFukui) {
                    $header["R_EAR_DB_IN"]          = "(装用時)聴力右平均dB";
                    if ($this->Properties["useEar4000Hz"] == "1") {
                        $header["R_EAR_DB_4000_IN"] = "(装用時)聴力右4000Hz";
                    }
                }
                $header["R_EAR_IN"]                 = "(装用時)聴力右状態コード";
            }
            $header["L_EAR_CANTMEASURE"]            = "聴力(左)測定困難";
            if (!$this->isFukui && ($this->isShien || $this->Properties["unEar_db"] != "1")) {
                if ($this->isKoma || ($this->Properties["printKenkouSindanIppan"] == "1" && $this->Properties["KenkouSindan_Ippan_Pattern"] == "1")) {
                    $header["L_EAR_DB_1000"]                = "聴力左1000Hz";
                } else {
                    $header["L_EAR_DB"]                     = "聴力左".$setEarTitle;
                }
                if ($this->Properties["useEar4000Hz"] == "1") {
                    $header["L_EAR_DB_4000"]            = "聴力左4000Hz";
                }
            }
            if (!$this->isKoma && ($this->Properties["printKenkouSindanIppan"] != "1" || $this->Properties["KenkouSindan_Ippan_Pattern"] != "1")) {
                $header["L_EAR"]                        = "聴力左状態コード";
            }
            if ($this->isShien) {
                if (!$this->isFukui) {
                    $header["L_EAR_DB_IN"]          = "(装用時)聴力左平均dB";
                    if ($this->Properties["useEar4000Hz"] == "1") {
                        $header["L_EAR_DB_4000_IN"] = "(装用時)聴力左4000Hz";
                    }
                }
                $header["L_EAR_IN"]                 = "(装用時)聴力左状態コード";
            }
            if ($Ippan == "0") {
                $header["ALBUMINURIA1CD"]           = "一次尿蛋白コード";
                $header["URICSUGAR1CD"]             = "一次尿糖コード";
                $header["URICBLEED1CD"]             = "一次尿潜血コード";
                $header["ALBUMINURIA2CD"]           = "二次尿蛋白コード";
                $header["URICSUGAR2CD"]             = "二次尿糖コード";
                $header["URICBLEED2CD"]             = "二次尿潜血コード";
                $header["URICOTHERTEST"]            = "尿その他の検査";
                $header["URI_ADVISECD"]             = "尿指導区分コード";
            }
            $header["NUTRITIONCD"]                  = "栄養状態コード";
            if ($Ippan == "1") {
                $header["NUTRITIONCD_REMARK"]       = "栄養状態所見";
            }
            if ($Ippan != "0") {
                $header["SPINERIBCD"]               = "脊柱胸郭四肢コード";
                $header["SPINERIBCD_REMARK"]        = "脊柱胸郭四肢所見";
            }
            $header["EYEDISEASECD"]                 = "目疾病及異常コード";
            if ($Ippan == "2") {
                $header["EYEDISEASECD2"]            = "目疾病及異常コード2";
                $header["EYEDISEASECD3"]            = "目疾病及異常コード3";
                $header["EYEDISEASECD4"]            = "目疾病及異常コード4";
            }
            $header["EYE_TEST_RESULT"]              = "眼科所見";
            if ($Ippan == "2") {
                $header["EYE_TEST_RESULT2"]         = "眼科所見2";
                $header["EYE_TEST_RESULT3"]         = "眼科所見3";
            }
            if ($Ippan == "1") {
                if (!$this->isMigagi) { // 宮城は色覚異常を使用しない
                    $header["EYEDISEASECD5"]            = "色覚異常コード";
                }
            }
            if ($Ippan == "0") {
                $header["SPINERIBCD"]               = "脊柱胸郭四肢コード";
                $header["SPINERIBCD_REMARK"]        = "脊柱胸郭四肢所見";
            }
            $header["NOSEDISEASECD"]                = "耳鼻咽頭疾患コード";
            if ($Ippan == "2") {
                $header["NOSEDISEASECD2"]           = "耳鼻咽頭疾患コード2";
                $header["NOSEDISEASECD3"]           = "耳鼻咽頭疾患コード3";
                $header["NOSEDISEASECD4"]           = "耳鼻咽頭疾患コード4";
            }
            $header["NOSEDISEASECD_REMARK"]         = "耳鼻咽頭疾患所見";
            if ($Ippan == "2") {
                $header["NOSEDISEASECD_REMARK2"]    = "耳鼻咽頭疾患所見2";
                $header["NOSEDISEASECD_REMARK3"]    = "耳鼻咽頭疾患所見3";
            }
            if ($Ippan == "1") {
                $header["NOSEDISEASECD5"]           = "耳疾患コード";
                $header["NOSEDISEASECD_REMARK1"]    = "耳疾患所見";
                $header["NOSEDISEASECD6"]           = "鼻・副鼻腔疾患コード";
                $header["NOSEDISEASECD_REMARK2"]    = "鼻・副鼻腔疾患所見";
                $header["NOSEDISEASECD7"]           = "口腔咽頭疾患・異常コード";
                $header["NOSEDISEASECD_REMARK3"]    = "口腔咽頭疾患・異常所見";
            }
            $header["SKINDISEASECD"]                = "皮膚疾患コード";
            if ($Ippan == "1" || $Ippan == "2") {
                $header["SKINDISEASECD_REMARK"]     = "皮膚疾患所見";
            }

            if ($this->Properties["printKenkouSindanIppan"] == "1" && $this->Properties["KenkouSindan_Ippan_Pattern"] == "1") {
                $header["OTHERDISEASECD"]               = "その他疾病及異常コード";
                if ($Ippan != "2") {
                    if (!$this->isMie) {
                        $header["OTHER_REMARK2"]            = "その他疾病及異常所見";
                    }
                }
                if ($Ippan == "0" || $Ippan == "1") {
                    $header["OTHER_ADVISECD"]           = "その他疾病指導区分コード";
                }
                if ($Ippan == "1" || $Ippan == "2") {
                    $header["OTHER_REMARK"]             = "その他疾病指導区分所見";
                }
                if ($Ippan == "2") {
                    $header["OTHER_REMARK2"]            = "その他疾病指導区分所見2";
                    $header["OTHER_REMARK3"]            = "その他疾病指導区分所見3";
                }
            }

            if ($this->field["SCHOOL_KIND"] == "H") {
                if ($Ippan != "0") {
                    $header["TB_FILMDATE"]          = "結核撮影日付";
                    $header["TB_FILMNO"]            = "結核フィルム番号";
                    $header["TB_REMARKCD"]          = "結核所見コード";
                    $header["TB_X_RAY"]             = "結核所見";
                    $header["TB_OTHERTESTCD"]       = "結核その他検査コード";
                }
                if ($Ippan == "2") {
                    $header["TB_OTHERTEST_REMARK1"] = "結核その他検査所見";
                }
            }
            if ($Ippan == "1") {
                $tb_namecd_label = ($this->isMigagi) ? "病名" : "疾病及び異常";
            } else {
                $tb_namecd_label = "病名";
            }
            if ($Ippan != "0") {
                $header["TB_NAMECD"]                = "結核{$tb_namecd_label}コード";
            }
            if (!$this->isMie) {
                $header["TB_NAME_REMARK1"]          = "結核{$tb_namecd_label}所見";
            }
            if ($Ippan != "0") {
                $header["TB_ADVISECD"]              = "結核指導区分コード";
            }
            if ($Ippan == "2") {
                $header["TB_ADVISE_REMARK1"]        = "結核指導区分所見";
            }
            $header["HEART_MEDEXAM"]                = "心臓臨床医学的検査コード";
            $header["HEART_MEDEXAM_REMARK"]         = "心臓臨床医学的検査";
            if ($this->isKoma) {
                $header["HEART_GRAPH_NO"]           = "心電図No";
            }
            $header["HEARTDISEASECD"]               = "心臓疾病及異常コード";
            $header["HEARTDISEASECD_REMARK"]        = "心臓疾病及異常所見";
            if ($Ippan == "0" || $Ippan == "1") {
                $header["MANAGEMENT_DIV"]           = "心臓管理区分";
            }
            if ($Ippan == "1") {
                $header["MANAGEMENT_REMARK"]        = "心臓管理所見";
            }
            if ($Ippan == "0") {
                if ($this->field["SCHOOL_KIND"] == "H") {
                    $header["TB_FILMDATE"]          = "結核撮影日付";
                    $header["TB_FILMNO"]            = "結核フィルム番号";
                    $header["TB_REMARKCD"]          = "結核所見コード";
                    $header["TB_X_RAY"]             = "結核所見";
                    $header["TB_OTHERTESTCD"]       = "結核その他検査コード";
                }
                $header["TB_NAMECD"]                = "結核{$tb_namecd_label}コード";
                $header["TB_ADVISECD"]              = "結核指導区分コード";
            }
            if ($Ippan != "0") {
                $header["ALBUMINURIA1CD"]           = "一次尿蛋白コード";
                $header["URICSUGAR1CD"]             = "一次尿糖コード";
                $header["URICBLEED1CD"]             = "一次尿潜血コード";
                if ($Ippan == 1 && $this->Properties["KenkouSindan_Ippan_Pattern"] == "1") {
                    $header["URICPH1"]                = "一次尿ＰＨ";
                }
                $header["ALBUMINURIA2CD"]           = "二次尿蛋白コード";
                $header["URICSUGAR2CD"]             = "二次尿糖コード";
                $header["URICBLEED2CD"]             = "二次尿潜血コード";
                if ($Ippan == 1 && $this->Properties["KenkouSindan_Ippan_Pattern"] == "1") {
                    $header["URICPH2"]                = "二次尿ＰＨ";
                }
                if ($Ippan == "2") {
                    $header["URICOTHERTESTCD"]      = "尿その他の検査コード";
                }
                if ($Ippan != "2") {
                    $header["URICOTHERTEST"]        = "尿その他の検査";
                }
                if ($Ippan == "1") {
                    $header["URI_ADVISECD"]         = "尿指導区分コード";
                }
            }
            if ($this->Properties["useParasite_".$this->field["SCHOOL_KIND"]] == "1") {
                $header["PARASITE"]                 = "寄生虫卵";
            }

            if ($this->Properties["printKenkouSindanIppan"] != "1" || $this->Properties["KenkouSindan_Ippan_Pattern"] != "1") {
                $header["OTHERDISEASECD"]               = "その他疾病及異常コード";
                if ($Ippan != "2") {
                    if (!$this->isMie) {
                        $header["OTHER_REMARK2"]            = "その他疾病及異常所見";
                    }
                }
                if ($Ippan == "0" || $Ippan == "1") {
                    $header["OTHER_ADVISECD"]           = "その他疾病指導区分コード";
                }
                if ($Ippan == "1" || $Ippan == "2") {
                    $header["OTHER_REMARK"]             = "その他疾病指導区分所見";
                }
                if ($Ippan == "2") {
                    $header["OTHER_REMARK2"]            = "その他疾病指導区分所見2";
                    $header["OTHER_REMARK3"]            = "その他疾病指導区分所見3";
                }
            }
            if ($this->isFukui) {// 福井は貧血
                $header["ANEMIA_REMARK"]            = "貧血所見";
                $header["HEMOGLOBIN"]               = "ヘモグロビン値";
            }
            $header["DOC_DATE"]                     = "学校医日付";
            if ($Ippan == "0" || $Ippan == "1") {
                if ($this->isMie) {
                    $header["DOC_CD"]                   = "学校医コード   ";
                }
                $header["DOC_REMARK"]               = "学校医所見";
            }
            if ($Ippan != "0") {
                $header["TREATCD"]                  = "事後処置コード";
                if (!$this->isMie) {
                    $header["TREATCD2"]                 = "事後処置2コード";
                    $header["TREAT_REMARK1"]            = "事後処置所見1";
                }
            }
            if ($Ippan == "2") {
                $header["TREAT_REMARK2"]            = "事後処置所見2";
                $header["TREAT_REMARK3"]            = "事後処置所見3";
            }
            $header["REMARK"]                       = "備考";
            if ($Ippan == "2") {
                $header["MESSAGE"]                  = "メッセージ";
            }
            $header["MEDICAL_HISTORY1"]             = "既往症1";
            $header["MEDICAL_HISTORY2"]             = "既往症2";
            $header["MEDICAL_HISTORY3"]             = "既往症3";
            $header["DIAGNOSIS_NAME"]               = "診断名";
            $header["GUIDE_DIV"]                    = "指導区分";
            $header["JOINING_SPORTS_CLUB"]          = "運動部活動";
            $header[$this->lastColumn]              = $this->lastColumn;

            //ヘッダー例
            $header_rei["CSVCD"]                    = "1";
            $header_rei["YEAR"]                     = CTRL_YEAR;
            $header_rei["DATE"]                     = CTRL_DATE;
            $header_rei["SCHREGNO"]                 = CTRL_YEAR."0001";
            $header_rei["GRADE"]                    = "１年";
            $header_rei["HR_CLASS"]                 = "１組";
            $header_rei["ATTENDNO"]                 = "001";
            $header_rei["NAME"]                     = "アルプ太郎";
            $header_rei["HEIGHT"]                   = "165.5";
            $header_rei["WEIGHT"]                   = "80.5";
            $header_rei["SITHEIGHT"]                = "75.5";
            $header_rei["NO_PRINT_OBESITY_INDEX"]   = "1";
            $header_rei["R_VISION_CANTMEASURE"]     = "1";
            $header_rei["L_VISION_CANTMEASURE"]     = "1";
            $header_rei["R_BAREVISION"]             = "0.50";
            $header_rei["R_BAREVISION_MARK"]        = "C";
            $header_rei["L_BAREVISION"]             = "0.70";
            $header_rei["L_BAREVISION_MARK"]        = "B";
            $header_rei["R_VISION"]                 = "1.50";
            $header_rei["R_VISION_MARK"]            = "A";
            $header_rei["L_VISION"]                 = "1.70";
            $header_rei["L_VISION_MARK"]            = "A";
            $header_rei["R_EAR_CANTMEASURE"]        = "1";
            $header_rei["R_EAR_DB"]                 = "20";
            $header_rei["R_EAR_DB_1000"]            = "01";
            $header_rei["R_EAR_DB_4000"]            = "25";
            $header_rei["R_EAR"]                    = "01";
            $header_rei["R_EAR_DB_IN"]              = "20";
            $header_rei["R_EAR_DB_4000_IN"]         = "25";
            $header_rei["R_EAR_IN"]                 = "01";
            $header_rei["L_EAR_CANTMEASURE"]        = "1";
            $header_rei["L_EAR_DB"]                 = "15";
            $header_rei["L_EAR_DB_1000"]            = "01";
            $header_rei["L_EAR_DB_4000"]            = "20";
            $header_rei["L_EAR"]                    = "02";
            $header_rei["L_EAR_DB_IN"]              = "20";
            $header_rei["L_EAR_DB_4000_IN"]         = "25";
            $header_rei["L_EAR_IN"]                 = "01";
            $header_rei["ALBUMINURIA1CD"]           = "01";
            $header_rei["URICSUGAR1CD"]             = "01";
            $header_rei["URICBLEED1CD"]             = "01";
            if ($Ippan == 1 && $this->Properties["KenkouSindan_Ippan_Pattern"] == "1") {
                $header_rei["URICPH1"]             = "1.2";
            }
            $header_rei["ALBUMINURIA2CD"]           = "01";
            $header_rei["URICSUGAR2CD"]             = "01";
            $header_rei["URICBLEED2CD"]             = "01";
            if ($Ippan == 1 && $this->Properties["KenkouSindan_Ippan_Pattern"] == "1") {
                $header_rei["URICPH2"]             = "1.1";
            }
            $header_rei["URICOTHERTESTCD"]          = "01";
            $header_rei["URICOTHERTEST"]            = "再検査済　異常なし";
            $header_rei["URI_ADVISECD"]             = "01";
            $header_rei["NUTRITIONCD"]              = "01";
            $header_rei["NUTRITIONCD_REMARK"]       = "栄養状態所見";
            $header_rei["SPINERIBCD"]               = "01";
            $header_rei["SPINERIBCD_REMARK"]        = "脊柱・胸郭・四肢所見";
            $header_rei["EYEDISEASECD"]             = "01";
            $header_rei["EYEDISEASECD2"]            = "01";
            $header_rei["EYEDISEASECD3"]            = "01";
            $header_rei["EYEDISEASECD4"]            = "01";
            $header_rei["EYE_TEST_RESULT"]          = "目の疾病及び異常所見";
            $header_rei["EYE_TEST_RESULT2"]         = "目の疾病及び異常所見2";
            $header_rei["EYE_TEST_RESULT3"]         = "目の疾病及び異常所見3";
            $header_rei["EYEDISEASECD5"]            = "01";
            $header_rei["NOSEDISEASECD"]            = "01";
            $header_rei["NOSEDISEASECD2"]           = "01";
            $header_rei["NOSEDISEASECD3"]           = "01";
            $header_rei["NOSEDISEASECD4"]           = "01";
            $header_rei["NOSEDISEASECD_REMARK"]     = "耳鼻咽頭疾患所見";
            $header_rei["NOSEDISEASECD5"]           = "01";
            $header_rei["NOSEDISEASECD_REMARK1"]    = "耳疾患所見";
            $header_rei["NOSEDISEASECD6"]           = "01";
            $header_rei["NOSEDISEASECD_REMARK2"]    = ($Ippan == "1") ? "鼻・副鼻腔疾患所見" : "耳鼻咽頭疾患所見2";
            $header_rei["NOSEDISEASECD7"]           = "01";
            $header_rei["NOSEDISEASECD_REMARK3"]    = ($Ippan == "1") ? "口腔咽頭疾患・異常所見" : "耳鼻咽頭疾患所見3";
            $header_rei["SKINDISEASECD"]            = "01";
            $header_rei["SKINDISEASECD_REMARK"]     = "皮膚疾患所見";
            $header_rei["TB_FILMDATE"]              = CTRL_DATE;
            $header_rei["TB_FILMNO"]                = substr("F123456789012345", 0, $this->tb_filmnoFieldSize);
            $header_rei["TB_REMARKCD"]              = "01";
            $header_rei["TB_X_RAY"]                 = "結核所見";
            $header_rei["TB_OTHERTESTCD"]           = "01";
            $header_rei["TB_OTHERTEST_REMARK1"]     = "結核その他検査所見";
            $header_rei["TB_NAMECD"]                = "01";
            if (!$this->isMie) {
                $header_rei["TB_NAME_REMARK1"]          = "結核{$tb_namecd_label}所見";
            }
            $header_rei["TB_ADVISECD"]              = "01";
            $header_rei["TB_ADVISE_REMARK1"]        = "結核指導区分所見";
            $header_rei["HEART_MEDEXAM"]            = "01";
            $header_rei["HEART_MEDEXAM_REMARK"]     = "不完全右脚ブロック、心雑音、不整脈など";
            $header_rei["HEART_GRAPH_NO"]           = "1234";
            $header_rei["HEARTDISEASECD"]           = "01";
            $header_rei["HEARTDISEASECD_REMARK"]    = "疾病及び異常";
            $header_rei["MANAGEMENT_DIV"]           = "01";
            $header_rei["MANAGEMENT_REMARK"]        = "心臓管理所見";
            $header_rei["PARASITE"]                 = "01";
            $header_rei["OTHERDISEASECD"]           = "01";
            $header_rei["OTHER_ADVISECD"]           = "01";
            $header_rei["OTHER_REMARK"]             = "その他疾病指導区分所見";
            if ($Ippan == "2") {
                $header_rei["OTHER_REMARK2"]            = "その他疾病指導区分所見2";
            } else {
                if (!$this->isMie) {
                    $header_rei["OTHER_REMARK2"]            = "その他疾病及異常所見";
                }
            }
            $header_rei["OTHER_REMARK3"]            = "その他疾病指導区分所見3";
            $header_rei["ANEMIA_REMARK"]            = "貧血所見";
            $header_rei["HEMOGLOBIN"]               = "12.0";
            $header_rei["DOC_DATE"]                 = CTRL_DATE;
            if ($this->isMie) {
                $header_rei["DOC_CD"]                   = "01";
            }
            $header_rei["DOC_REMARK"]               = "食事療法";
            $header_rei["TREATCD"]                  = "01";
            if (!$this->isMie) {
                $header_rei["TREATCD2"]                 = "02";
                $header_rei["TREAT_REMARK1"]            = "事後処置所見1";
            }
            $header_rei["TREAT_REMARK2"]            = "事後処置所見2";
            $header_rei["TREAT_REMARK3"]            = "事後処置所見3";
            $header_rei["REMARK"]                   = "食事療法";
            $header_rei["MESSAGE"]                  = "メッセージ";
            $header_rei["MEDICAL_HISTORY1"]         = "01";
            $header_rei["MEDICAL_HISTORY2"]         = "02";
            $header_rei["MEDICAL_HISTORY3"]         = "03";
            $header_rei["DIAGNOSIS_NAME"]           = "診断名";
            $header_rei["GUIDE_DIV"]                = "1";
            $header_rei["JOINING_SPORTS_CLUB"]      = "1";
            $header_rei[$this->lastColumn]          = $this->lastColumn;
        } else { //歯・口腔
            //ヘッダー
            $header["CSVCD"]                = "※CSVCD";
            $header["YEAR"]                 = "※年度";
            $header["TOOTH_DATE"]           = "※健診実施日";
            $header["SCHREGNO"]             = "※学籍番号";
            $header["GRADE"]                = "学年";
            $header["HR_CLASS"]             = "組";
            $header["ATTENDNO"]             = "出席番号";
            $header["NAME"]                 = "氏名";
            if (!($Ippan == "1" && $this->Properties["KenkouSindan_Ippan_Pattern"] == "1")) {
                $header["JAWS_JOINTCD2"]        = "顎関節コード";
            }
            if ($Ippan == "2"  || $Ippan == "3" || ($Ippan == "1" && ($this->z010name1 === "mieken" || $this->Properties["KenkouSindan_Ippan_Pattern"] == "1"))) {
                if ($this->z010name1 === "kumamoto" && $this->field["SCHOOL_KIND"] == "H") {
                    if ($this->field["OUTPUT"] == "1") {
                        $header["JAWS_JOINTCD"]     = "歯列・咬合コード";
                    } else {
                        $chkYear  = substr($this->field["YEAR"], 0, 4);
                        $chkGrade = substr($this->field["GRADE_HR_CLASS"], 0, 2);

                        if ((2015 < $chkYear && $chkYear < 2021) && $chkGrade == "") {
                            $header["JAWS_JOINTCD"]     = "歯列コード";
                        } else {
                            $header["JAWS_JOINTCD"]     = "歯列・咬合コード";
                        }
                        if (2016 > $chkYear) {
                            $header["JAWS_JOINTCD"]     = "歯列コード";
                        }
                        $minGrade = knjf140aQuery::getMinGrade($this);
                        $setGrade = ($chkYear > 2022) ? 0: $chkYear - 2016 + $minGrade;
                        if ($setGrade > 0 && $chkGrade > sprintf("%02d", $setGrade)) {
                            $header["JAWS_JOINTCD"]     = "歯列コード";
                        }
                    }
                } else {
                    $header["JAWS_JOINTCD"]     = "歯列コード";
                }
            } else {
                $header["JAWS_JOINTCD"]     = "歯列・咬合コード";
            }
            if ($Ippan == "2"  || $Ippan == "3" || ($Ippan == "1" && ($this->z010name1 === "mieken" || $this->Properties["KenkouSindan_Ippan_Pattern"] == "1"))) {
                if ($this->z010name1 === "kumamoto" && $this->field["SCHOOL_KIND"] == "H") {
                    if ($this->field["OUTPUT"] == "1") {
                    } else {
                        if ((2015 < $chkYear && $chkYear < 2021) && $chkGrade == "") {
                            $header["JAWS_JOINTCD3"]     = "咬合コード";
                        }
                        if (2016 > $chkYear) {
                            $header["JAWS_JOINTCD3"]     = "咬合コード";
                        }
                        if ($setGrade > 0 && $chkGrade > sprintf("%02d", $setGrade)) {
                            $header["JAWS_JOINTCD3"]     = "咬合コード";
                        }
                    }
                } else {
                    $header["JAWS_JOINTCD3"]    = "咬合コード";
                }
            }
            if ($Ippan == "1" && $this->Properties["KenkouSindan_Ippan_Pattern"] == "1") {
                $header["JAWS_JOINTCD2"]        = "顎関節コード";
            }
            $header["PLAQUECD"]             = "歯垢".(($Ippan == "1" && $this->Properties["KenkouSindan_Ippan_Pattern"] == "1") ? "付着" : "状態")."コード";
            $header["GUMCD"]                = "歯肉状態コード";
            if ($Ippan != "2") {
                if (!$this->isMigagi && !($Ippan == "1" && $this->Properties["KenkouSindan_Ippan_Pattern"] == "1")) { // 宮城またはパターンプロパティ="1"(城東)は歯石沈着を使用しない
                    $header["CALCULUS"]         = "歯石沈着";
                }
            }
            if (!$this->isMigagi) { // 宮城は矯正を使用しない
                $header["ORTHODONTICS"]         = "矯正の有無";
            }
            $header["UP_R_BABY5"]           = "乳歯 上 右E";
            $header["UP_R_BABY4"]           = "乳歯 上 右D";
            $header["UP_R_BABY3"]           = "乳歯 上 右C";
            $header["UP_R_BABY2"]           = "乳歯 上 右B";
            $header["UP_R_BABY1"]           = "乳歯 上 右A";
            $header["UP_L_BABY1"]           = "乳歯 上 左A";
            $header["UP_L_BABY2"]           = "乳歯 上 左B";
            $header["UP_L_BABY3"]           = "乳歯 上 左C";
            $header["UP_L_BABY4"]           = "乳歯 上 左D";
            $header["UP_L_BABY5"]           = "乳歯 上 左E";
            $header["LW_R_BABY5"]           = "乳歯 下 右E";
            $header["LW_R_BABY4"]           = "乳歯 下 右D";
            $header["LW_R_BABY3"]           = "乳歯 下 右C";
            $header["LW_R_BABY2"]           = "乳歯 下 右B";
            $header["LW_R_BABY1"]           = "乳歯 下 右A";
            $header["LW_L_BABY1"]           = "乳歯 下 左A";
            $header["LW_L_BABY2"]           = "乳歯 下 左B";
            $header["LW_L_BABY3"]           = "乳歯 下 左C";
            $header["LW_L_BABY4"]           = "乳歯 下 左D";
            $header["LW_L_BABY5"]           = "乳歯 下 左E";
            $header["BABYTOOTH"]            = "乳歯現在数";
            $header["REMAINBABYTOOTH"]      = "乳歯未処置数";
            $header["TREATEDBABYTOOTH"]     = "乳歯処置数";
            $header["BRACK_BABYTOOTH"]      = "要注意乳歯数";
            $header["UP_R_ADULT8"]          = "永久歯 上 右8";
            $header["UP_R_ADULT7"]          = "永久歯 上 右7";
            $header["UP_R_ADULT6"]          = "永久歯 上 右6";
            $header["UP_R_ADULT5"]          = "永久歯 上 右5";
            $header["UP_R_ADULT4"]          = "永久歯 上 右4";
            $header["UP_R_ADULT3"]          = "永久歯 上 右3";
            $header["UP_R_ADULT2"]          = "永久歯 上 右2";
            $header["UP_R_ADULT1"]          = "永久歯 上 右1";
            $header["UP_L_ADULT1"]          = "永久歯 上 左1";
            $header["UP_L_ADULT2"]          = "永久歯 上 左2";
            $header["UP_L_ADULT3"]          = "永久歯 上 左3";
            $header["UP_L_ADULT4"]          = "永久歯 上 左4";
            $header["UP_L_ADULT5"]          = "永久歯 上 左5";
            $header["UP_L_ADULT6"]          = "永久歯 上 左6";
            $header["UP_L_ADULT7"]          = "永久歯 上 左7";
            $header["UP_L_ADULT8"]          = "永久歯 上 左8";
            $header["LW_R_ADULT8"]          = "永久歯 下 右8";
            $header["LW_R_ADULT7"]          = "永久歯 下 右7";
            $header["LW_R_ADULT6"]          = "永久歯 下 右6";
            $header["LW_R_ADULT5"]          = "永久歯 下 右5";
            $header["LW_R_ADULT4"]          = "永久歯 下 右4";
            $header["LW_R_ADULT3"]          = "永久歯 下 右3";
            $header["LW_R_ADULT2"]          = "永久歯 下 右2";
            $header["LW_R_ADULT1"]          = "永久歯 下 右1";
            $header["LW_L_ADULT1"]          = "永久歯 下 左1";
            $header["LW_L_ADULT2"]          = "永久歯 下 左2";
            $header["LW_L_ADULT3"]          = "永久歯 下 左3";
            $header["LW_L_ADULT4"]          = "永久歯 下 左4";
            $header["LW_L_ADULT5"]          = "永久歯 下 左5";
            $header["LW_L_ADULT6"]          = "永久歯 下 左6";
            $header["LW_L_ADULT7"]          = "永久歯 下 左7";
            $header["LW_L_ADULT8"]          = "永久歯 下 左8";
            $header["ADULTTOOTH"]           = "永久歯数";
            $header["REMAINADULTTOOTH"]     = "永久歯未処置数";
            $header["TREATEDADULTTOOTH"]    = "永久歯処置数";
            $header["LOSTADULTTOOTH"]       = "永久歯喪失数";
            $header["BRACK_ADULTTOOTH"]     = "要観察歯数";
            if ($Ippan != "3") {
                $header["OTHERDISEASECD"]   = "その他疾病及び異常コード";
            }
            $header["OTHERDISEASE"]         = "その他疾病及び異常";
            if ($this->isMigagi) {
                $header["OTHERDISEASECD3"]  = "その他疾病及び異常２コード";
                $header["OTHERDISEASECD4"]  = "その他疾病及び異常３コード";
            }
            if ($this->is_f020_otherdisese_hyouji2) {
                $header["OTHERDISEASECD3"]  = "その他疾病及び異常２コード";
                $header["OTHERDISEASE3"]    = "その他疾病及び異常２";
                $header["OTHERDISEASECD4"]  = "その他疾病及び異常３コード";
                $header["OTHERDISEASE4"]    = "その他疾病及び異常３";
            }
            if ($Ippan != "2") {
                if (!$this->isMigagi) {
                    $header["OTHERDISEASECD2"]  = "口腔の疾病及び異常コード";
                }
                $header["OTHERDISEASE2"]    = "口腔の疾病及び異常";
                if ($this->is_f020_otherdisese_hyouji) {
                    $header["OTHERDISEASE_REMARK1"]    = "口腔の疾病及び異常コード2";
                    $header["OTHERDISEASE_REMARK2"]    = "口腔の疾病及び異常2";
                    $header["OTHERDISEASE_REMARK3"]    = "口腔の疾病及び異常コード3";
                    $header["OTHERDISEASE_REMARK4"]    = "口腔の疾病及び異常3";
                }
            }
            if ($Ippan == "0" || $Ippan == "1") {
                $header["DENTISTREMARKCD"]  = "学校歯科医所見コード";
                if (!$this->isMie) {
                    $header["DENTISTREMARK"]    = "学校歯科医所見";
                }
                if ($this->is_f020_dentistremark_hyouji) {
                    $header["DENTISTREMARK_REMARK1"]    = "学校歯科医所見コード2";
                    $header["DENTISTREMARK_REMARK2"]    = "学校歯科医所見2";
                    $header["DENTISTREMARK_REMARK3"]    = "学校歯科医所見コード3";
                    $header["DENTISTREMARK_REMARK4"]    = "学校歯科医所見3";
                }
            }
            if ($Ippan == "2" || $Ippan == "3") {
                $header["DENTISTREMARK_CO"] = "学校歯科医所見 CO";
                $header["DENTISTREMARK_GO"] = "学校歯科医所見 GO";
                $header["DENTISTREMARK_G"]  = "学校歯科医所見 G";
            }
            $header["DENTISTREMARKDATE"]    = "学校歯科医所見日付";
            if ($Ippan != "3") {
                $header["DENTISTTREATCD"]   = "学校歯科医事後処置コード";
            }
            if (!$this->isMie) {
                $header["DENTISTTREAT"]     = "学校歯科医事後処置";
            }
            if ($Ippan == "2") {
                $header["DENTISTTREAT2"]    = "学校歯科医事後処置2";
                $header["DENTISTTREAT3"]    = "学校歯科医事後処置3";
            }
            $header[$this->lastColumn]      = $this->lastColumn;

            //ヘッダー例
            $header_rei["CSVCD"]                = "2";
            $header_rei["YEAR"]                 = CTRL_YEAR;
            $header_rei["TOOTH_DATE"]           = CTRL_DATE;
            $header_rei["SCHREGNO"]             = CTRL_YEAR."0001";
            $header_rei["GRADE"]                = "１年";
            $header_rei["HR_CLASS"]             = "１組";
            $header_rei["ATTENDNO"]             = "001";
            $header_rei["NAME"]                 = "アルプ太郎";
            $header_rei["JAWS_JOINTCD2"]        = "01";
            $header_rei["JAWS_JOINTCD"]         = "01";
            $header_rei["JAWS_JOINTCD3"]        = "01";
            $header_rei["PLAQUECD"]             = "02";
            $header_rei["GUMCD"]                = "01";
            $header_rei["CALCULUS"]             = "01";
            $header_rei["ORTHODONTICS"]         = "1";
            $header_rei["UP_R_BABY5"]           = "01";
            $header_rei["UP_R_BABY4"]           = "02";
            $header_rei["UP_R_BABY3"]           = "03";
            $header_rei["UP_R_BABY2"]           = "04";
            $header_rei["UP_R_BABY1"]           = "05";
            $header_rei["UP_L_BABY1"]           = "06";
            $header_rei["UP_L_BABY2"]           = "01";
            $header_rei["UP_L_BABY3"]           = "02";
            $header_rei["UP_L_BABY4"]           = "03";
            $header_rei["UP_L_BABY5"]           = "04";
            $header_rei["LW_R_BABY5"]           = "05";
            $header_rei["LW_R_BABY4"]           = "06";
            $header_rei["LW_R_BABY3"]           = "01";
            $header_rei["LW_R_BABY2"]           = "02";
            $header_rei["LW_R_BABY1"]           = "03";
            $header_rei["LW_L_BABY1"]           = "04";
            $header_rei["LW_L_BABY2"]           = "05";
            $header_rei["LW_L_BABY3"]           = "06";
            $header_rei["LW_L_BABY4"]           = "01";
            $header_rei["LW_L_BABY5"]           = "02";
            $header_rei["BABYTOOTH"]            = "8";
            $header_rei["REMAINBABYTOOTH"]      = "1";
            $header_rei["TREATEDBABYTOOTH"]     = "2";
            $header_rei["BRACK_BABYTOOTH"]      = "5";
            $header_rei["UP_R_ADULT8"]          = "01";
            $header_rei["UP_R_ADULT7"]          = "02";
            $header_rei["UP_R_ADULT6"]          = "03";
            $header_rei["UP_R_ADULT5"]          = "04";
            $header_rei["UP_R_ADULT4"]          = "05";
            $header_rei["UP_R_ADULT3"]          = "06";
            $header_rei["UP_R_ADULT2"]          = "01";
            $header_rei["UP_R_ADULT1"]          = "02";
            $header_rei["UP_L_ADULT1"]          = "03";
            $header_rei["UP_L_ADULT2"]          = "04";
            $header_rei["UP_L_ADULT3"]          = "05";
            $header_rei["UP_L_ADULT4"]          = "06";
            $header_rei["UP_L_ADULT5"]          = "01";
            $header_rei["UP_L_ADULT6"]          = "02";
            $header_rei["UP_L_ADULT7"]          = "03";
            $header_rei["UP_L_ADULT8"]          = "04";
            $header_rei["LW_R_ADULT8"]          = "05";
            $header_rei["LW_R_ADULT7"]          = "06";
            $header_rei["LW_R_ADULT6"]          = "01";
            $header_rei["LW_R_ADULT5"]          = "02";
            $header_rei["LW_R_ADULT4"]          = "03";
            $header_rei["LW_R_ADULT3"]          = "04";
            $header_rei["LW_R_ADULT2"]          = "05";
            $header_rei["LW_R_ADULT1"]          = "06";
            $header_rei["LW_L_ADULT1"]          = "01";
            $header_rei["LW_L_ADULT2"]          = "02";
            $header_rei["LW_L_ADULT3"]          = "03";
            $header_rei["LW_L_ADULT4"]          = "04";
            $header_rei["LW_L_ADULT5"]          = "05";
            $header_rei["LW_L_ADULT6"]          = "06";
            $header_rei["LW_L_ADULT7"]          = "01";
            $header_rei["LW_L_ADULT8"]          = "02";
            $header_rei["ADULTTOOTH"]           = "20";
            $header_rei["REMAINADULTTOOTH"]     = "3";
            $header_rei["TREATEDADULTTOOTH"]    = "5";
            $header_rei["LOSTADULTTOOTH"]       = "2";
            $header_rei["BRACK_ADULTTOOTH"]     = "3";
            $header_rei["OTHERDISEASECD"]       = "99";
            $header_rei["OTHERDISEASE"]         = "全角20文字";
            if ($this->isMigagi) {
                $header_rei["OTHERDISEASECD3"]      = "99";
                $header_rei["OTHERDISEASECD4"]      = "99";
            }
            if ($this->is_f020_otherdisese_hyouji2) {
                $header_rei["OTHERDISEASECD3"]  = "99";
                $header_rei["OTHERDISEASE3"]    = "全角20文字";
                $header_rei["OTHERDISEASECD4"]  = "99";
                $header_rei["OTHERDISEASE4"]    = "全角20文字";
            }
            $header_rei["OTHERDISEASECD2"]      = "99";
            $header_rei["OTHERDISEASE2"]        = "全角20文字";
            if ($this->is_f020_otherdisese_hyouji) {
                $header_rei["OTHERDISEASE_REMARK1"]      = "99";
                $header_rei["OTHERDISEASE_REMARK2"]      = "全角20文字";
                $header_rei["OTHERDISEASE_REMARK3"]      = "99";
                $header_rei["OTHERDISEASE_REMARK4"]      = "全角20文字";
            }
            $header_rei["DENTISTREMARKCD"]      = "02";
            if (!$this->isMie) {
                $header_rei["DENTISTREMARK"]        = "全角20文字";
            }
            if ($this->is_f020_dentistremark_hyouji) {
                $header_rei["DENTISTREMARK_REMARK1"]      = "02";
                $header_rei["DENTISTREMARK_REMARK2"]      = "全角20文字";
                $header_rei["DENTISTREMARK_REMARK3"]      = "02";
                $header_rei["DENTISTREMARK_REMARK4"]      = "全角20文字";
            }
            $header_rei["DENTISTREMARK_CO"]     = "99";
            $header_rei["DENTISTREMARK_GO"]     = "1";
            $header_rei["DENTISTREMARK_G"]      = "1";
            $header_rei["DENTISTREMARKDATE"]    = CTRL_DATE;
            $header_rei["DENTISTTREATCD"]       = "01";
            if (!$this->isMie) {
                if ($Ippan == "2" || $Ippan == "3") {
                    $header_rei["DENTISTTREAT"]         = "全角10文字";
                } else {
                    $header_rei["DENTISTTREAT"]         = "全角20文字";
                }
            }
            $header_rei["DENTISTTREAT2"]        = "全角10文字";
            $header_rei["DENTISTTREAT3"]        = "全角10文字";
            $header_rei[$this->lastColumn]      = $this->lastColumn;
        }

        $contents  = ""; //クリア

        if ($this->field["OUTPUT"]=="1") { //ヘッダ
            $contents .= implode($header, ",")."\n";
            $sep = "";
            foreach ($header as $key => $val) {
                $contents .= $sep.$header_rei[$key];
                $sep = ",";
            }
            $contents .= "\n";
        } elseif ($this->field["OUTPUT"]=="3") {        //エラー
            $duplicateHeader = array("YEAR"          => "年度",
                                     "SCHREGNO"      => "学籍番号",
                                     "DET_SEQ"       => "健康診断SEQ",
                                     "TOOTH_SEQ"     => "健康診断SEQ"
                                    );
            if ($this->field["CSVCD"] == "1") {
                $tables = array("1" => "MEDEXAM_DET_DAT", "2" => "MEDEXAM_DET_DETAIL_DAT", "3" => "MEDEXAM_HDAT");
            } else {
                $tables = array("1" => "MEDEXAM_TOOTH_DAT", "2" => "MEDEXAM_TOOTH_DETAIL_DAT", "3" => "MEDEXAM_HDAT");
            }
            $dupTable = $this->getDuplicateErrorHeader(PROGRAMID, $tables);
            if (sizeof($dupTable) > 0) {
                $headerErr = $this->duplicateCsvHeader($dupTable, $duplicateHeader);
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
                $query = knjf140aQuery::selectCsvQuery();
            } elseif ($this->field["OUTPUT"]=="4") { //データ
                $query = knjf140aQuery::getselectDataSQL($this);
            }
            //SQL文発行
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($this->field["OUTPUT"]=="4") { //データ
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

        if ($data!="") {
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
        if ($data!="") {
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
        if ($this->field["SHORI_MEI"]=="1") {
            //一般-----
            //歯・口腔-----
            if ($data["SCHREGNO"]!="") {
                if (strlen($data["SCHREGNO"]) < 8) {
                    $data["SCHREGNO"] = sprintf("%08d", $data["SCHREGNO"]);
                }
            }

            //一般-----
            if ($this->field["CSVCD"] == "1") {
                if ($this->isFukui) {
                    if ($data["R_BAREVISION"]!="") {
                        $data_arr = array();
                        $data_arr = explode(".", $data["R_BAREVISION"]);
                        $data["R_BAREVISION"] = $data_arr[0] . "." . sprintf("%-03s", $data_arr[1]);
                    }
                    if ($data["L_BAREVISION"]!="") {
                        $data_arr = array();
                        $data_arr = explode(".", $data["L_BAREVISION"]);
                        $data["L_BAREVISION"] = $data_arr[0] . "." . sprintf("%-03s", $data_arr[1]);
                    }
                    if ($data["R_VISION"]!="") {
                        $data_arr = array();
                        $data_arr = explode(".", $data["R_VISION"]);
                        $data["R_VISION"] = $data_arr[0] . "." . sprintf("%-03s", $data_arr[1]);
                    }
                    if ($data["L_VISION"]!="") {
                        $data_arr = array();
                        $data_arr = explode(".", $data["L_VISION"]);
                        $data["L_VISION"] = $data_arr[0] . "." . sprintf("%-03s", $data_arr[1]);
                    }
                }
                if ($data["HEMOGLOBIN"]!="") {
                    $data_arr = array();
                    $data_arr = explode(".", $data["HEMOGLOBIN"]);
                    $data["HEMOGLOBIN"] = $data_arr[0] . "." . sprintf("%-01s", $data_arr[1]);
                }
                if ($data["R_EAR"]!="") {
                    if (strlen($data["R_EAR"])          < 2) {
                        $data["R_EAR"]          = sprintf("%02d", $data["R_EAR"]);
                    }
                }
                if ($data["L_EAR"]!="") {
                    if (strlen($data["L_EAR"])          < 2) {
                        $data["L_EAR"]          = sprintf("%02d", $data["L_EAR"]);
                    }
                }
                if ($data["ALBUMINURIA1CD"]!="") {
                    if (strlen($data["ALBUMINURIA1CD"]) < 2) {
                        $data["ALBUMINURIA1CD"] = sprintf("%02d", $data["ALBUMINURIA1CD"]);
                    }
                }
                if ($data["URICSUGAR1CD"]!="") {
                    if (strlen($data["URICSUGAR1CD"])   < 2) {
                        $data["URICSUGAR1CD"]   = sprintf("%02d", $data["URICSUGAR1CD"]);
                    }
                }
                if ($data["URICBLEED1CD"]!="") {
                    if (strlen($data["URICBLEED1CD"])   < 2) {
                        $data["URICBLEED1CD"]   = sprintf("%02d", $data["URICBLEED1CD"]);
                    }
                }
                if ($this->Properties["printKenkouSindanIppan"] == 1 && $this->Properties["KenkouSindan_Ippan_Pattern"] == "1" && $data["URICPH1"] != "") {
                    if (strlen($data["URICPH1"])   < 4) {
                        $data["URICPH1"]   = sprintf("%04d", $data["URICPH1"]);
                    }
                }
                if ($data["ALBUMINURIA2CD"]!="") {
                    if (strlen($data["ALBUMINURIA2CD"]) < 2) {
                        $data["ALBUMINURIA2CD"] = sprintf("%02d", $data["ALBUMINURIA2CD"]);
                    }
                }
                if ($data["URICSUGAR2CD"]!="") {
                    if (strlen($data["URICSUGAR2CD"])   < 2) {
                        $data["URICSUGAR2CD"]   = sprintf("%02d", $data["URICSUGAR2CD"]);
                    }
                }
                if ($data["URICBLEED2CD"]!="") {
                    if (strlen($data["URICBLEED2CD"])   < 2) {
                        $data["URICBLEED2CD"]   = sprintf("%02d", $data["URICBLEED2CD"]);
                    }
                }
                if ($this->Properties["printKenkouSindanIppan"] == 1 && $this->Properties["KenkouSindan_Ippan_Pattern"] == "1" && $data["URICPH2"] != "") {
                    if (strlen($data["URICPH2"])   < 4) {
                        $data["URICPH2"]   = sprintf("%04d", $data["URICPH2"]);
                    }
                }
                if ($data["URICOTHERTESTCD"]!="") {
                    if (strlen($data["URICOTHERTESTCD"])   < 2) {
                        $data["URICOTHERTESTCD"]   = sprintf("%02d", $data["URICOTHERTESTCD"]);
                    }
                }
                if ($data["URI_ADVISECD"]!="") {
                    if (strlen($data["URI_ADVISECD"])   < 2) {
                        $data["URI_ADVISECD"]   = sprintf("%02d", $data["URI_ADVISECD"]);
                    }
                }
                if ($data["NUTRITIONCD"]!="") {
                    if (strlen($data["NUTRITIONCD"])    < 2) {
                        $data["NUTRITIONCD"]    = sprintf("%02d", $data["NUTRITIONCD"]);
                    }
                }
                if ($data["SPINERIBCD"]!="") {
                    if (strlen($data["SPINERIBCD"])     < 2) {
                        $data["SPINERIBCD"]     = sprintf("%02d", $data["SPINERIBCD"]);
                    }
                }
                if ($data["EYEDISEASECD"]!="") {
                    if (strlen($data["EYEDISEASECD"])   < 2) {
                        $data["EYEDISEASECD"]   = sprintf("%02d", $data["EYEDISEASECD"]);
                    }
                }
                if ($data["EYEDISEASECD2"]!="") {
                    if (strlen($data["EYEDISEASECD2"])   < 2) {
                        $data["EYEDISEASECD2"]   = sprintf("%02d", $data["EYEDISEASECD2"]);
                    }
                }
                if ($data["EYEDISEASECD3"]!="") {
                    if (strlen($data["EYEDISEASECD3"])   < 2) {
                        $data["EYEDISEASECD3"]   = sprintf("%02d", $data["EYEDISEASECD3"]);
                    }
                }
                if ($data["EYEDISEASECD4"]!="") {
                    if (strlen($data["EYEDISEASECD4"])   < 2) {
                        $data["EYEDISEASECD4"]   = sprintf("%02d", $data["EYEDISEASECD4"]);
                    }
                }
                if ($data["EYEDISEASECD5"]!="") {
                    if (strlen($data["EYEDISEASECD5"])   < 2) {
                        $data["EYEDISEASECD5"]   = sprintf("%02d", $data["EYEDISEASECD5"]);
                    }
                }
                if ($data["NOSEDISEASECD"]!="") {
                    if (strlen($data["NOSEDISEASECD"])  < 2) {
                        $data["NOSEDISEASECD"]  = sprintf("%02d", $data["NOSEDISEASECD"]);
                    }
                }
                if ($data["NOSEDISEASECD2"]!="") {
                    if (strlen($data["NOSEDISEASECD2"])  < 2) {
                        $data["NOSEDISEASECD2"]  = sprintf("%02d", $data["NOSEDISEASECD2"]);
                    }
                }
                if ($data["NOSEDISEASECD3"]!="") {
                    if (strlen($data["NOSEDISEASECD3"])  < 2) {
                        $data["NOSEDISEASECD3"]  = sprintf("%02d", $data["NOSEDISEASECD4"]);
                    }
                }
                if ($data["NOSEDISEASECD4"]!="") {
                    if (strlen($data["NOSEDISEASECD4"])  < 2) {
                        $data["NOSEDISEASECD4"]  = sprintf("%02d", $data["NOSEDISEASECD4"]);
                    }
                }
                if ($data["NOSEDISEASECD5"]!="") {
                    if (strlen($data["NOSEDISEASECD5"])  < 2) {
                        $data["NOSEDISEASECD5"]  = sprintf("%02d", $data["NOSEDISEASECD5"]);
                    }
                }
                if ($data["NOSEDISEASECD6"]!="") {
                    if (strlen($data["NOSEDISEASECD6"])  < 2) {
                        $data["NOSEDISEASECD6"]  = sprintf("%02d", $data["NOSEDISEASECD6"]);
                    }
                }
                if ($data["NOSEDISEASECD7"]!="") {
                    if (strlen($data["NOSEDISEASECD7"])  < 2) {
                        $data["NOSEDISEASECD7"]  = sprintf("%02d", $data["NOSEDISEASECD7"]);
                    }
                }
                if ($data["SKINDISEASECD"]!="") {
                    if (strlen($data["SKINDISEASECD"])  < 2) {
                        $data["SKINDISEASECD"]  = sprintf("%02d", $data["SKINDISEASECD"]);
                    }
                }
                if ($data["HEART_MEDEXAM"]!="") {
                    if (strlen($data["HEART_MEDEXAM"])  < 2) {
                        $data["HEART_MEDEXAM"]  = sprintf("%02d", $data["HEART_MEDEXAM"]);
                    }
                }
                if ($data["HEARTDISEASECD"]!="") {
                    if (strlen($data["HEARTDISEASECD"]) < 2) {
                        $data["HEARTDISEASECD"] = sprintf("%02d", $data["HEARTDISEASECD"]);
                    }
                }
                if ($data["MANAGEMENT_DIV"]!="") {
                    if (strlen($data["MANAGEMENT_DIV"]) < 2) {
                        $data["MANAGEMENT_DIV"] = sprintf("%02d", $data["MANAGEMENT_DIV"]);
                    }
                }
                if ($data["TB_REMARKCD"]!="") {
                    if (strlen($data["TB_REMARKCD"])    < 2) {
                        $data["TB_REMARKCD"]    = sprintf("%02d", $data["TB_REMARKCD"]);
                    }
                }
                if ($data["TB_OTHERTESTCD"]!="") {
                    if (strlen($data["TB_OTHERTESTCD"]) < 2) {
                        $data["TB_OTHERTESTCD"] = sprintf("%02d", $data["TB_OTHERTESTCD"]);
                    }
                }
                if ($data["TB_NAMECD"]!="") {
                    if (strlen($data["TB_NAMECD"])      < 2) {
                        $data["TB_NAMECD"]      = sprintf("%02d", $data["TB_NAMECD"]);
                    }
                }
                if ($data["TB_ADVISECD"]!="") {
                    if (strlen($data["TB_ADVISECD"])    < 2) {
                        $data["TB_ADVISECD"]    = sprintf("%02d", $data["TB_ADVISECD"]);
                    }
                }
                if ($data["PARASITE"]!="") {
                    if (strlen($data["PARASITE"])       < 2) {
                        $data["PARASITE"]       = sprintf("%02d", $data["PARASITE"]);
                    }
                }
                if ($data["OTHERDISEASECD"]!="") {
                    if (strlen($data["OTHERDISEASECD"]) < 2) {
                        $data["OTHERDISEASECD"] = sprintf("%02d", $data["OTHERDISEASECD"]);
                    }
                }
                if ($data["OTHER_ADVISECD"]!="") {
                    if (strlen($data["OTHER_ADVISECD"]) < 2) {
                        $data["OTHER_ADVISECD"] = sprintf("%02d", $data["OTHER_ADVISECD"]);
                    }
                }
                if ($this->isMie && $data["DOC_CD"]!="") {
                    if (strlen($data["DOC_CD"]) < 2) {
                        $data["DOC_CD"] = sprintf("%02d", $data["DOC_CD"]);
                    }
                }
                if ($data["TREATCD"]!="") {
                    if (strlen($data["TREATCD"])        < 2) {
                        $data["TREATCD"]        = sprintf("%02d", $data["TREATCD"]);
                    }
                }
                if ($data["TREATCD2"]!="") {
                    if (strlen($data["TREATCD2"])        < 2) {
                        $data["TREATCD2"]        = sprintf("%02d", $data["TREATCD2"]);
                    }
                }
                //既往歴はマスタ登録で0埋めてない学校が多いので、0埋めしない。
                //歯・口腔-----
            } else {
                if ($data["JAWS_JOINTCD"]!="") {
                    if (strlen($data["JAWS_JOINTCD"]) < 2) {
                        $data["JAWS_JOINTCD"] = sprintf("%02d", $data["JAWS_JOINTCD"]);
                    }
                }
                if ($data["JAWS_JOINTCD3"]!="") { //熊本仕様
                    if (strlen($data["JAWS_JOINTCD3"]) < 2) {
                        $data["JAWS_JOINTCD3"] = sprintf("%02d", $data["JAWS_JOINTCD3"]);
                    }
                }
                if ($data["JAWS_JOINTCD2"]!="") {
                    if (strlen($data["JAWS_JOINTCD2"]) < 2) {
                        $data["JAWS_JOINTCD2"] = sprintf("%02d", $data["JAWS_JOINTCD2"]);
                    }
                }
                if ($data["PLAQUECD"]!="") {
                    if (strlen($data["PLAQUECD"]) < 2) {
                        $data["PLAQUECD"] = sprintf("%02d", $data["PLAQUECD"]);
                    }
                }
                if ($data["GUMCD"]!="") {
                    if (strlen($data["GUMCD"]) < 2) {
                        $data["GUMCD"] = sprintf("%02d", $data["GUMCD"]);
                    }
                }
                if ($data["CALCULUS"]!="") {
                    if (strlen($data["CALCULUS"]) < 2) {
                        $data["CALCULUS"] = sprintf("%02d", $data["CALCULUS"]);
                    }
                }
                //乳歯 上
                if ($data["UP_R_BABY5"]!="") {
                    if (strlen($data["UP_R_BABY5"]) < 2) {
                        $data["UP_R_BABY5"] = sprintf("%02d", $data["UP_R_BABY5"]);
                    }
                }
                if ($data["UP_R_BABY4"]!="") {
                    if (strlen($data["UP_R_BABY4"]) < 2) {
                        $data["UP_R_BABY4"] = sprintf("%02d", $data["UP_R_BABY4"]);
                    }
                }
                if ($data["UP_R_BABY3"]!="") {
                    if (strlen($data["UP_R_BABY3"]) < 2) {
                        $data["UP_R_BABY3"] = sprintf("%02d", $data["UP_R_BABY3"]);
                    }
                }
                if ($data["UP_R_BABY2"]!="") {
                    if (strlen($data["UP_R_BABY2"]) < 2) {
                        $data["UP_R_BABY2"] = sprintf("%02d", $data["UP_R_BABY2"]);
                    }
                }
                if ($data["UP_R_BABY1"]!="") {
                    if (strlen($data["UP_R_BABY1"]) < 2) {
                        $data["UP_R_BABY1"] = sprintf("%02d", $data["UP_R_BABY1"]);
                    }
                }
                if ($data["UP_L_BABY1"]!="") {
                    if (strlen($data["UP_L_BABY1"]) < 2) {
                        $data["UP_L_BABY1"] = sprintf("%02d", $data["UP_L_BABY1"]);
                    }
                }
                if ($data["UP_L_BABY2"]!="") {
                    if (strlen($data["UP_L_BABY2"]) < 2) {
                        $data["UP_L_BABY2"] = sprintf("%02d", $data["UP_L_BABY2"]);
                    }
                }
                if ($data["UP_L_BABY3"]!="") {
                    if (strlen($data["UP_L_BABY3"]) < 2) {
                        $data["UP_L_BABY3"] = sprintf("%02d", $data["UP_L_BABY3"]);
                    }
                }
                if ($data["UP_L_BABY4"]!="") {
                    if (strlen($data["UP_L_BABY4"]) < 2) {
                        $data["UP_L_BABY4"] = sprintf("%02d", $data["UP_L_BABY4"]);
                    }
                }
                if ($data["UP_L_BABY5"]!="") {
                    if (strlen($data["UP_L_BABY5"]) < 2) {
                        $data["UP_L_BABY5"] = sprintf("%02d", $data["UP_L_BABY5"]);
                    }
                }
                //乳歯 下
                if ($data["LW_R_BABY5"]!="") {
                    if (strlen($data["LW_R_BABY5"]) < 2) {
                        $data["LW_R_BABY5"] = sprintf("%02d", $data["LW_R_BABY5"]);
                    }
                }
                if ($data["LW_R_BABY4"]!="") {
                    if (strlen($data["LW_R_BABY4"]) < 2) {
                        $data["LW_R_BABY4"] = sprintf("%02d", $data["LW_R_BABY4"]);
                    }
                }
                if ($data["LW_R_BABY3"]!="") {
                    if (strlen($data["LW_R_BABY3"]) < 2) {
                        $data["LW_R_BABY3"] = sprintf("%02d", $data["LW_R_BABY3"]);
                    }
                }
                if ($data["LW_R_BABY2"]!="") {
                    if (strlen($data["LW_R_BABY2"]) < 2) {
                        $data["LW_R_BABY2"] = sprintf("%02d", $data["LW_R_BABY2"]);
                    }
                }
                if ($data["LW_R_BABY1"]!="") {
                    if (strlen($data["LW_R_BABY1"]) < 2) {
                        $data["LW_R_BABY1"] = sprintf("%02d", $data["LW_R_BABY1"]);
                    }
                }
                if ($data["LW_L_BABY1"]!="") {
                    if (strlen($data["LW_L_BABY1"]) < 2) {
                        $data["LW_L_BABY1"] = sprintf("%02d", $data["LW_L_BABY1"]);
                    }
                }
                if ($data["LW_L_BABY2"]!="") {
                    if (strlen($data["LW_L_BABY2"]) < 2) {
                        $data["LW_L_BABY2"] = sprintf("%02d", $data["LW_L_BABY2"]);
                    }
                }
                if ($data["LW_L_BABY3"]!="") {
                    if (strlen($data["LW_L_BABY3"]) < 2) {
                        $data["LW_L_BABY3"] = sprintf("%02d", $data["LW_L_BABY3"]);
                    }
                }
                if ($data["LW_L_BABY4"]!="") {
                    if (strlen($data["LW_L_BABY4"]) < 2) {
                        $data["LW_L_BABY4"] = sprintf("%02d", $data["LW_L_BABY4"]);
                    }
                }
                if ($data["LW_L_BABY5"]!="") {
                    if (strlen($data["LW_L_BABY5"]) < 2) {
                        $data["LW_L_BABY5"] = sprintf("%02d", $data["LW_L_BABY5"]);
                    }
                }
                //永久歯 上
                if ($data["UP_R_ADULT8"]!="") {
                    if (strlen($data["UP_R_ADULT8"]) < 2) {
                        $data["UP_R_ADULT8"] = sprintf("%02d", $data["UP_R_ADULT8"]);
                    }
                }
                if ($data["UP_R_ADULT7"]!="") {
                    if (strlen($data["UP_R_ADULT7"]) < 2) {
                        $data["UP_R_ADULT7"] = sprintf("%02d", $data["UP_R_ADULT7"]);
                    }
                }
                if ($data["UP_R_ADULT6"]!="") {
                    if (strlen($data["UP_R_ADULT6"]) < 2) {
                        $data["UP_R_ADULT6"] = sprintf("%02d", $data["UP_R_ADULT6"]);
                    }
                }
                if ($data["UP_R_ADULT5"]!="") {
                    if (strlen($data["UP_R_ADULT5"]) < 2) {
                        $data["UP_R_ADULT5"] = sprintf("%02d", $data["UP_R_ADULT5"]);
                    }
                }
                if ($data["UP_R_ADULT4"]!="") {
                    if (strlen($data["UP_R_ADULT4"]) < 2) {
                        $data["UP_R_ADULT4"] = sprintf("%02d", $data["UP_R_ADULT4"]);
                    }
                }
                if ($data["UP_R_ADULT3"]!="") {
                    if (strlen($data["UP_R_ADULT3"]) < 2) {
                        $data["UP_R_ADULT3"] = sprintf("%02d", $data["UP_R_ADULT3"]);
                    }
                }
                if ($data["UP_R_ADULT2"]!="") {
                    if (strlen($data["UP_R_ADULT2"]) < 2) {
                        $data["UP_R_ADULT2"] = sprintf("%02d", $data["UP_R_ADULT2"]);
                    }
                }
                if ($data["UP_R_ADULT1"]!="") {
                    if (strlen($data["UP_R_ADULT1"]) < 2) {
                        $data["UP_R_ADULT1"] = sprintf("%02d", $data["UP_R_ADULT1"]);
                    }
                }
                if ($data["UP_L_ADULT1"]!="") {
                    if (strlen($data["UP_L_ADULT1"]) < 2) {
                        $data["UP_L_ADULT1"] = sprintf("%02d", $data["UP_L_ADULT1"]);
                    }
                }
                if ($data["UP_L_ADULT2"]!="") {
                    if (strlen($data["UP_L_ADULT2"]) < 2) {
                        $data["UP_L_ADULT2"] = sprintf("%02d", $data["UP_L_ADULT2"]);
                    }
                }
                if ($data["UP_L_ADULT3"]!="") {
                    if (strlen($data["UP_L_ADULT3"]) < 2) {
                        $data["UP_L_ADULT3"] = sprintf("%02d", $data["UP_L_ADULT3"]);
                    }
                }
                if ($data["UP_L_ADULT4"]!="") {
                    if (strlen($data["UP_L_ADULT4"]) < 2) {
                        $data["UP_L_ADULT4"] = sprintf("%02d", $data["UP_L_ADULT4"]);
                    }
                }
                if ($data["UP_L_ADULT5"]!="") {
                    if (strlen($data["UP_L_ADULT5"]) < 2) {
                        $data["UP_L_ADULT5"] = sprintf("%02d", $data["UP_L_ADULT5"]);
                    }
                }
                if ($data["UP_L_ADULT6"]!="") {
                    if (strlen($data["UP_L_ADULT6"]) < 2) {
                        $data["UP_L_ADULT6"] = sprintf("%02d", $data["UP_L_ADULT6"]);
                    }
                }
                if ($data["UP_L_ADULT7"]!="") {
                    if (strlen($data["UP_L_ADULT7"]) < 2) {
                        $data["UP_L_ADULT7"] = sprintf("%02d", $data["UP_L_ADULT7"]);
                    }
                }
                if ($data["UP_L_ADULT8"]!="") {
                    if (strlen($data["UP_L_ADULT8"]) < 2) {
                        $data["UP_L_ADULT8"] = sprintf("%02d", $data["UP_L_ADULT8"]);
                    }
                }
                //永久歯 下
                if ($data["LW_R_ADULT8"]!="") {
                    if (strlen($data["LW_R_ADULT8"]) < 2) {
                        $data["LW_R_ADULT8"] = sprintf("%02d", $data["LW_R_ADULT8"]);
                    }
                }
                if ($data["LW_R_ADULT7"]!="") {
                    if (strlen($data["LW_R_ADULT7"]) < 2) {
                        $data["LW_R_ADULT7"] = sprintf("%02d", $data["LW_R_ADULT7"]);
                    }
                }
                if ($data["LW_R_ADULT6"]!="") {
                    if (strlen($data["LW_R_ADULT6"]) < 2) {
                        $data["LW_R_ADULT6"] = sprintf("%02d", $data["LW_R_ADULT6"]);
                    }
                }
                if ($data["LW_R_ADULT5"]!="") {
                    if (strlen($data["LW_R_ADULT5"]) < 2) {
                        $data["LW_R_ADULT5"] = sprintf("%02d", $data["LW_R_ADULT5"]);
                    }
                }
                if ($data["LW_R_ADULT4"]!="") {
                    if (strlen($data["LW_R_ADULT4"]) < 2) {
                        $data["LW_R_ADULT4"] = sprintf("%02d", $data["LW_R_ADULT4"]);
                    }
                }
                if ($data["LW_R_ADULT3"]!="") {
                    if (strlen($data["LW_R_ADULT3"]) < 2) {
                        $data["LW_R_ADULT3"] = sprintf("%02d", $data["LW_R_ADULT3"]);
                    }
                }
                if ($data["LW_R_ADULT2"]!="") {
                    if (strlen($data["LW_R_ADULT2"]) < 2) {
                        $data["LW_R_ADULT2"] = sprintf("%02d", $data["LW_R_ADULT2"]);
                    }
                }
                if ($data["LW_R_ADULT1"]!="") {
                    if (strlen($data["LW_R_ADULT1"]) < 2) {
                        $data["LW_R_ADULT1"] = sprintf("%02d", $data["LW_R_ADULT1"]);
                    }
                }
                if ($data["LW_L_ADULT1"]!="") {
                    if (strlen($data["LW_L_ADULT1"]) < 2) {
                        $data["LW_L_ADULT1"] = sprintf("%02d", $data["LW_L_ADULT1"]);
                    }
                }
                if ($data["LW_L_ADULT2"]!="") {
                    if (strlen($data["LW_L_ADULT2"]) < 2) {
                        $data["LW_L_ADULT2"] = sprintf("%02d", $data["LW_L_ADULT2"]);
                    }
                }
                if ($data["LW_L_ADULT3"]!="") {
                    if (strlen($data["LW_L_ADULT3"]) < 2) {
                        $data["LW_L_ADULT3"] = sprintf("%02d", $data["LW_L_ADULT3"]);
                    }
                }
                if ($data["LW_L_ADULT4"]!="") {
                    if (strlen($data["LW_L_ADULT4"]) < 2) {
                        $data["LW_L_ADULT4"] = sprintf("%02d", $data["LW_L_ADULT4"]);
                    }
                }
                if ($data["LW_L_ADULT5"]!="") {
                    if (strlen($data["LW_L_ADULT5"]) < 2) {
                        $data["LW_L_ADULT5"] = sprintf("%02d", $data["LW_L_ADULT5"]);
                    }
                }
                if ($data["LW_L_ADULT6"]!="") {
                    if (strlen($data["LW_L_ADULT6"]) < 2) {
                        $data["LW_L_ADULT6"] = sprintf("%02d", $data["LW_L_ADULT6"]);
                    }
                }
                if ($data["LW_L_ADULT7"]!="") {
                    if (strlen($data["LW_L_ADULT7"]) < 2) {
                        $data["LW_L_ADULT7"] = sprintf("%02d", $data["LW_L_ADULT7"]);
                    }
                }
                if ($data["LW_L_ADULT8"]!="") {
                    if (strlen($data["LW_L_ADULT8"]) < 2) {
                        $data["LW_L_ADULT8"] = sprintf("%02d", $data["LW_L_ADULT8"]);
                    }
                }
                //その他疾病及異常コード
                if ($data["OTHERDISEASECD"]!="") {
                    if (strlen($data["OTHERDISEASECD"]) < 2) {
                        $data["OTHERDISEASECD"] = sprintf("%02d", $data["OTHERDISEASECD"]);
                    }
                }
                if ($data["OTHERDISEASECD3"]!="") {
                    if (strlen($data["OTHERDISEASECD3"]) < 2) {
                        $data["OTHERDISEASECD3"] = sprintf("%02d", $data["OTHERDISEASECD3"]);
                    }
                }
                if ($data["OTHERDISEASECD4"]!="") {
                    if (strlen($data["OTHERDISEASECD4"]) < 2) {
                        $data["OTHERDISEASECD4"] = sprintf("%02d", $data["OTHERDISEASECD4"]);
                    }
                }
                if ($data["OTHERDISEASECD2"]!="") {
                    if (strlen($data["OTHERDISEASECD2"]) < 2) {
                        $data["OTHERDISEASECD2"] = sprintf("%02d", $data["OTHERDISEASECD2"]);
                    }
                }
                if ($this->is_f020_otherdisese_hyouji) {
                    if ($data["OTHERDISEASE_REMARK1"]!="") {
                        if (strlen($data["OTHERDISEASE_REMARK1"]) < 2) {
                            $data["OTHERDISEASE_REMARK1"] = sprintf("%02d", $data["OTHERDISEASE_REMARK1"]);
                        }
                    }
                    if ($data["OTHERDISEASE_REMARK3"]!="") {
                        if (strlen($data["OTHERDISEASE_REMARK3"]) < 2) {
                            $data["OTHERDISEASE_REMARK3"] = sprintf("%02d", $data["OTHERDISEASE_REMARK3"]);
                        }
                    }
                }
                //学校歯科医所見コード
                if ($data["DENTISTREMARKCD"]!="") {
                    if (strlen($data["DENTISTREMARKCD"]) < 2) {
                        $data["DENTISTREMARKCD"] = sprintf("%02d", $data["DENTISTREMARKCD"]);
                    }
                }
                if ($this->is_f020_dentistremark_hyouji) {
                    //学校歯科医所見コード2
                    if ($data["DENTISTREMARK_REMARK1"]!="") {
                        if (strlen($data["DENTISTREMARK_REMARK1"]) < 2) {
                            $data["DENTISTREMARK_REMARK1"] = sprintf("%02d", $data["DENTISTREMARK_REMARK1"]);
                        }
                    }
                    //学校歯科医所見コード3
                    if ($data["DENTISTREMARK_REMARK3"]!="") {
                        if (strlen($data["DENTISTREMARK_REMARK3"]) < 2) {
                            $data["DENTISTREMARK_REMARK3"] = sprintf("%02d", $data["DENTISTREMARK_REMARK3"]);
                        }
                    }
                }
                //学校歯科医事後処置コード
                if ($data["DENTISTTREATCD"]!="") {
                    if (strlen($data["DENTISTTREATCD"]) < 2) {
                        $data["DENTISTTREATCD"] = sprintf("%02d", $data["DENTISTTREATCD"]);
                    }
                }
            }//csvcd
        //削除
        } else {
            if ($data["SCHREGNO"]!="") {
                if (strlen($data["SCHREGNO"]) < 8) {
                    $data["SCHREGNO"] = sprintf("%08d", $data["SCHREGNO"]);
                }
            }
        }

        return $data;
    }

    //視力の文字チェック
    public function siryokumojiCheck(&$mark, $suuji, $db, $fieldName)
    {
        //広工大は数値が名称マスタ設定の範囲内に存在するかチェック
        if ($this->isHirokoudai) {
            $visionChkHiro = true;
            $checkHiroArr = array();
            $query = knjf140aQuery::getVisionMark($this);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $checkHiroArr[$row["NAMECD2"]] = $row;
            }

            foreach ($checkHiroArr as $nameCd2 => $arrVal) {
                if ($suuji != '') {
                    $visionChkHiro = false;
                    if ('A' == mb_convert_kana($arrVal["NAMECD2"], 'r')) {
                        if ($arrVal["NAMESPARE1"] != '' && $suuji >= $arrVal["NAMESPARE1"]) {
                            $mark = mb_convert_kana($arrVal["NAMECD2"], 'r');
                            $visionChkHiro = true;
                            return;
                        }
                    } else {
                        if (($arrVal["NAMESPARE1"] <= $suuji) && ($suuji <= $arrVal["ABBV3"])) {
                            $mark = mb_convert_kana($arrVal["NAMECD2"], 'r');
                            $visionChkHiro = true;
                            return;
                        }
                    }
                }
            }
            if (!$visionChkHiro) {
                return true;
            }
        } else {
            if ($mark && $this->Properties["printKenkouSindanIppan"] != "2") {
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
            } elseif ($mark && $this->Properties["printKenkouSindanIppan"] == "2") {
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
                    case "未":
                        $mark = "未";
                        break;
                    default:
                        return true;
                        break;
                }
            } elseif ($suuji) {
                $query = knjf140aQuery::getSiryokuMark($suuji); //視力も文字取得
                $mark = $db->getOne($query);
            }
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
