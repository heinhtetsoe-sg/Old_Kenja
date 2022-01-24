<?php

require_once('for_php7.php');

class knja124psModel extends Model {

    var $cmd;
    var $hash = array();      //検証用のハッシュを収める
    var $cert_bin;  //証明書(クッキー)
    var $passwd;    //パスワード(クッキー)
    var $exe_type;  //処理タイプ CHARGE：担当、PRINCIPAL：校長
    var $schoolCd;
    var $left_year;
    var $left_semester;
    var $setUrl;
    var $rndm;
    var $gosign = array();
    var $signature = array();
    var $hr_class;
    var $chk_data = array();
    var $chk_all;

    function knja124psModel() {}

    function init() {
        $cert_asc = VARS::COOKIE("I9e58bUn");
        $this->cert_bin = base64_decode($cert_asc);
        $passwrd = VARS::COOKIE("hoj3RG8t");

        if (VARS::get("PROGRAMID") != "") {
            unset($this->left_year);
            unset($this->left_semester);
        }

        $this->cmd = VARS::request("cmd");
        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if (!$this->cmd) {
            unset($this->left_year);
            unset($this->left_semester);
        }

        if (VARS::get("RNDM") != "") {
            $this->rndm = VARS::get("RNDM");
            $this->passwd=common::passwdDecode($passwrd, $this->rndm);
        }
        else if (VARS::post("RNDM") != "") {
            $this->rndm = VARS::post("RNDM");
            $this->passwd=common::passwdDecode($passwrd, $this->rndm);
        }
        if ($this->cmd == 'sslExe') {
            $schregnoArray = explode(",", VARS::POST("SCHREGNO"));
            $gosignArray = explode(",", VARS::POST("GOSIGN"));
            $signatureArray = explode(",", VARS::POST("SIGNATURE"));
            foreach ($schregnoArray as $key => $val) {
                $this->gosign[$val] = $gosignArray[$key];
                $this->signature[$val] = $signatureArray[$key];
            }
        } else {
            $this->gosign = array();
            $this->signature = array();
        }
        if (VARS::get("EXE_TYPE") != "") {
            $this->exe_type = VARS::get("EXE_TYPE");
        }
        if (VARS::get("setUrl") != "") {
            $this->setUrl = VARS::get("setUrl");
            $this->setUrl = str_replace("PROGRAMID", "PID", $this->setUrl);
        }



        $this->left_year = CTRL_YEAR;
        $this->left_semester = CTRL_SEMESTER;
        $this->hr_class = VARS::post("HR_CLASS");
        $this->chk_data = VARS::post("CHK_DATA");
        $this->chk_all = VARS::post("CHK_ALL");



        $db = Query::dbCheckOut();

        $query = knja124psQuery::getNameMst("Z010");
        $nmMstRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $this->schoolCd = $nmMstRow["NAME2"];

        Query::dbCheckIn($db);

    }

    function getSendAuth() {
        if(VARS::get("SEND_AUTH")){
            $this->sendAuth = VARS::get("SEND_AUTH");
            $this->getSendAuth = "1";
        }
    }

    //署名されたデータの検証、更新
    function &getShomeiModel() {
        if (strlen($this->passwd)==0) {
            if (strlen($this->rndm)==0) {
                $this->setMessage("署名情報が足りません。");
                return;
            }
            $passwrd = VARS::COOKIE("hoj3RG8t");
            if (strlen($passwrd)==0) {
                $this->setMessage("署名情報が読込めません。");
                return;
            }
            $this->passwd=common::passwdDecode($passwrd, $this->rndm);
            if (strlen($this->passwd)==0) {
                $this->setMessage("署名情報が解読出来ません[".$passwrd.']['.$this->rndm.']');
                return;
            }
        }

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        if (isset($this->chk_data)) {
            $shomeiFlg = false;
            foreach ($this->chk_data as $key => $schregno) {
                $rtnFlg = $this->shomei($db, $schregno);
                if ($rtnFlg) $shomeiFlg = true;
            }

            if ($shomeiFlg) {
                $this->setMessage("署名に成功しました。");
            } else {
                $this->setMessage("署名に失敗しました。");
            }
        }

        $db->commit();
        Query::dbCheckIn($db);

    }

    //署名されたデータの検証、更新
    function &shomei($db, $schregno) {

        //HTRAINREMARK_P_DAT
        $query = knja124psQuery::getTrainRow($this, $schregno);
        $tRow = $db->getRow($query, DB_FETCHMODE_ASSOC);

        //BEHAVIOR_DAT
        $behavior = "";
        $resultb = $db->query(knja124psQuery::getBehavior($this, $schregno));
        while($rowb = $resultb->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $behavior .= $rowb["DIV"].$rowb["CODE"].$rowb["ANNUAL"];
        }

        //SCHREG_STUDYREC_DAT
        $query = knja124psQuery::getStudyRec($this, $schregno);
        $result = $db->query($query);
        $study = "";
        while ($studyRow = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
	        //教育課程対応
	        if ($this->Properties["useCurriculumcd"] == '1') {
		    	$study .= $studyRow["CLASSCD"].$studyRow["SCHOOL_KIND"].$studyRow["CURRICULUM_CD"].$studyRow["SUBCLASSCD"].$studyRow["CLASSNAME"].$studyRow["SUBCLASSNAME"].
		    	$studyRow["VALUATION"].$studyRow["GET_CREDIT"].$studyRow["ADD_CREDIT"].$studyRow["COMP_CREDIT"];
	        } else {
	            $study .= $studyRow["CLASSCD"].$studyRow["SUBCLASSCD"].$studyRow["CLASSNAME"].$studyRow["SUBCLASSNAME"].
	            $studyRow["VALUATION"].$studyRow["GET_CREDIT"].$studyRow["ADD_CREDIT"].$studyRow["COMP_CREDIT"];
	        }
        }

        //SCHREG_ATTENDREC_DAT
        $attend = $db->getRow(knja124psQuery::getAttendRec($this, $schregno), DB_FETCHMODE_ASSOC);

        //現在データのHASHを取得
        $makeHash = $this->makeHash($tRow, $behavior, $study, $attend);

        //表示データと現在データのHASHを比較
        if ($makeHash == $this->hash[$schregno]) {
            if ($this->update($db, $schregno)) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }

    }

    //アップデート
    function &update($db, $schregno) {

        $query = knja124psQuery::getSeqNo($this);
        $seq = $db->getOne($query);
        $seq++;

        if ($this->exe_type == "CHARGE") {
            return $this->chageUpdate($db, $seq, $schregno);
        } else {
            return $this->principalUpdate($db, $seq, $schregno);
        }

    }

    //担任更新
    function chageUpdate($db, $seq, $schregno) {

        if (openssl_pkcs12_read( $this->cert_bin, $p12cert, $this->passwd)) {
            $privatekey = $p12cert["pkey"]; //プライベートキー取出し
            $pkeyid = openssl_get_privatekey($privatekey);
            if ($pkeyid) {
                // 署名
                openssl_sign($this->hash[$schregno], $signature_bin, $pkeyid);
                // base64に変換
                $signature = base64_encode($signature_bin);
                // メモリからキーを開放
                openssl_free_key($pkeyid);
            } else {
                return false;
            }
        }
        else {
            return false;
        }

        knja124psQuery::getInsertSql($db, $this, $seq, $this->hash[$schregno], $signature, $schregno);

        return true;
    }

    //P12ファイル読込（証明書）
    function getPem()
    {

        $this->existPem = "";
        //PEM形式個人証明書ファイルを読込む
        $filename = DOCUMENTROOT."/image/CERTIFICATE".$this->schoolCd."/certifiCate/".STAFFCD.".p12";

        if (!file_exists($filename)) {
            $this->existPem = "NONE";
        } else {
            $fd = fopen($filename, "r");
            $cert_p12 = fread($fd, filesize($filename));
            fclose($fd);
            if (openssl_pkcs12_read( $cert_p12, $p12cert, $this->passwd)) {
                return $p12cert["cert"];
            }
        }
        return null;
    }

    //校長更新
    function principalUpdate($db, $seq, $schregno) {

        $cert_pem  = "";                    // 検証処理PEM形式個人証明書読込みエリア
        $cert_pem  = $this->getPem();       // サーバの証明書
        //USBでの署名データ取出し
        if (strcmp($this->gosign[$schregno],"OK")!=0) {
            $this->gosign[$schregno]="";
            return false;
		}

        /** 検証処理 **/
        $hashHash = sha1($this->hash[$schregno]);

        //DBの署名データをbase64_decode(sha1にする)してファイルに書き込む
        $signature_bin = base64_decode($this->signature[$schregno]);

        //公開鍵取出し
        $pubkeyid = openssl_pkey_get_public($cert_pem);
        if ($pubkeyid) {
            // 署名が正しいかどうかを確認
            if (openssl_public_decrypt($signature_bin, $digest, $pubkeyid)==TRUE) {
                //実行結果ファイル(sha1をsha1した値)を読み込んで所見(sha1をsha1した値)と比較
                if (trim($digest) == trim($hashHash))
                    $ret = 1;
            }
            // メモリからキーを開放
            openssl_free_key($pubkeyid);
        }
        else {
            $ret = -1;
            $this->setMessage("Fail openssl_get_publickey");
        }
        if ($ret != 1) {
            return false;
        }

        //署名データ取出し
        $query = knja124psQuery::getOpinionsSignature($this, $schregno);
        $opinionsSignature = $db->getRow($query, DB_FETCHMODE_ASSOC);
        //担任の署名データOPINION(sha1)を読み込んで所見(sha1)と比較
        if ($opinionsSignature["CHAGE_OPI"] != $this->hash[$schregno]) {
            return false;
        }
        //実行結果０は、１とする。(とりあえず)
        $ret = $ret == 0 ? 1 : $ret;
        //検証結果
        if ($ret != 1) {
            return false;
        }
        knja124psQuery::getInsUpdSql($db, $this, $seq, $this->hash[$schregno], $this->signature[$schregno], $schregno);

        return true;
    }

    //ハッシュ値を作る
    function makeHash($tRow, $behavior, $study, $attend) {

        $data = $tRow["FOREIGNLANGACT1"].$tRow["FOREIGNLANGACT2"].$tRow["FOREIGNLANGACT3"].$tRow["FOREIGNLANGACT4"].$tRow["TOTALREMARK"].
                $tRow["TOTALSTUDYACT"].$tRow["VIEWREMARK"].$tRow["TOTALSTUDYVAL"].$tRow["ATTENDREC_REMARK"].$behavior.
                $tRow["SPECIALACTREMARK"].$study.$attend["CLASSDAYS"].$attend["OFFDAYS"].$attend["ABSENT"].
                $attend["SUSPEND"].$attend["MOURNING"].$attend["ABROAD"].$attend["REQUIREPRESENT"].$attend["SICK"].
                $attend["ACCIDENTNOTICE"].$attend["NOACCIDENTNOTICE"].$attend["PRESENT"];

        return sha1($data);
    }
}
?>
