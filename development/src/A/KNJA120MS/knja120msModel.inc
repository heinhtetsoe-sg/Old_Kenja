<?php

require_once('for_php7.php');

class knja120msModel extends Model {

    var $cmd;
    var $schregno = "";
    var $name = "";
    var $hash;      //検証用のハッシュを収める
    var $cert_bin;  //証明書(クッキー)
    var $passwd;    //パスワード(クッキー)
    var $exe_type;  //処理タイプ CHARGE：担当、PRINCIPAL：校長
    var $schoolCd;
    var $left_year;
    var $left_semester;
    var $setUrl;
    var $rndm;
    var $gosign;
    var $signature;

    function knja120msModel() {}

    function init() {
        $cert_asc = VARS::COOKIE("I9e58bUn");
        $this->cert_bin = base64_decode($cert_asc);
        $passwrd = VARS::COOKIE("hoj3RG8t");

        if (VARS::get("PROGRAMID") != "") {
            unset($this->schregno);
            unset($this->name);
            unset($this->left_year);
            unset($this->left_semester);
        }

        $this->cmd = VARS::request("cmd");
        //プロパティファイルの読込み
        $this->getPropertiesAll();
        
        if (!$this->cmd) {
            unset($this->schregno);
            unset($this->name);
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
        $this->gosign = VARS::POST("GOSIGN");
        $this->signature = VARS::POST("SIGNATURE");
        if (VARS::get("SCHREGNO") != "") {
            $this->schregno = VARS::get("SCHREGNO");
        }
        if (VARS::get("NAME") != "") {
            $this->name = VARS::get("NAME");
        }
        if (VARS::get("EXP_YEAR") != "") {
            $this->left_year = VARS::get("EXP_YEAR");
        }
        if (VARS::get("EXP_SEMESTER") != "") {
            $this->left_semester = VARS::get("EXP_SEMESTER");
        }
        if (VARS::get("EXE_TYPE") != "") {
            $this->exe_type = VARS::get("EXE_TYPE");
        }
        if (VARS::get("setUrl") != "") {
            $this->setUrl = VARS::get("setUrl");
            $this->setUrl = str_replace("PROGRAMID", "PID", $this->setUrl);
        }

        //リストの年組コンボが変更されたとき
        if (VARS::get("init")) {
            unset($this->schregno);
            unset($this->name);
            unset($this->left_year);
            unset($this->left_semester);
        }

        $db = Query::dbCheckOut();

        $query = knja120msQuery::getNameMst("Z010");
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

    //差戻し
    function &getSasimodosiModel() {

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //署名クリア
        $query = knja120msQuery::getDeleteWk($this);
        $db->query($query);
        $this->setMessage("署名を差戻しました。");

        $db->commit();
        Query::dbCheckIn($db);

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

        //HTRAINREMARK_P_DAT
        $query = knja120msQuery::getTrainRow($this);
        $tRow = $db->getRow($query, DB_FETCHMODE_ASSOC);

        //SCHREG_STUDYREC_DAT
        $query = knja120msQuery::getStudyRec($this);
        $result = $db->query($query);
        $study = "";
        //教育課程対応
        if ($this->Properties["useCurriculumcd"] == '1') {
	        while ($studyRow = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
	            $study .= $studyRow["CLASSCD"].$studyRow["SCHOOL_KIND"].$studyRow["CURRICULUM_CD"].$studyRow["SUBCLASSCD"].$studyRow["CLASSNAME"].$studyRow["SUBCLASSNAME"].
	            $studyRow["VALUATION"].$studyRow["GET_CREDIT"].$studyRow["ADD_CREDIT"].$studyRow["COMP_CREDIT"];
	        }
        } else {
	        while ($studyRow = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
	            $study .= $studyRow["CLASSCD"].$studyRow["SUBCLASSCD"].$studyRow["CLASSNAME"].$studyRow["SUBCLASSNAME"].
	            $studyRow["VALUATION"].$studyRow["GET_CREDIT"].$studyRow["ADD_CREDIT"].$studyRow["COMP_CREDIT"];
	        }
        }

        //HR_ATTEND_DAT
        $attend = $db->getOne(knja120msQuery::getHrAttend($this));

        //現在データのHASHを取得
        $makeHash = $this->makeHash($tRow, $study, $attend);

        //表示データと現在データのHASHを比較
        if ($makeHash == $this->hash) {
            if ($this->update($db)) {
                $this->setMessage("署名に成功しました。");
            } else {
                $this->setMessage("署名に失敗しました。");
            }
        } else {
            $this->setMessage("署名に失敗しました。\\n所見データに変更があります。");
        }

        $db->commit();
        Query::dbCheckIn($db);

    }

    //アップデート
    function &update($db) {

        $query = knja120msQuery::getSeqNo($this);
        $seq = $db->getOne($query);
        $seq++;

        if ($this->exe_type == "CHARGE") {
            return $this->chageUpdate($db, $seq);
        } else {
            return $this->principalUpdate($db, $seq);
        }

    }

    //担任更新
    function chageUpdate($db, $seq) {

        if (openssl_pkcs12_read( $this->cert_bin, $p12cert, $this->passwd)) {
            $privatekey = $p12cert["pkey"]; //プライベートキー取出し
            $pkeyid = openssl_get_privatekey($privatekey);
            if ($pkeyid) {
                // 署名
                openssl_sign($this->hash, $signature_bin, $pkeyid);
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

        knja120msQuery::getInsertSql($db, $this, $seq, $this->hash, $signature);

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
    function principalUpdate($db, $seq) {

        $cert_pem  = "";                    // 検証処理PEM形式個人証明書読込みエリア
        $cert_pem  = $this->getPem();       // サーバの証明書
        //USBでの署名データ取出し
        if (strcmp($this->gosign,"OK")!=0) {
            unset($this->passwd);
            $this->gosign="";
            $this->setMessage("認証処理に失敗しました。");
		}

        /** 検証処理 **/
        $hashHash = sha1($this->hash);

        //DBの署名データをbase64_decode(sha1にする)してファイルに書き込む
        $signature_bin = base64_decode($this->signature);

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
        $query = knja120msQuery::getOpinionsSignature($this);
        $opinionsSignature = $db->getRow($query, DB_FETCHMODE_ASSOC);
        //担任の署名データOPINION(sha1)を読み込んで所見(sha1)と比較
        if ($opinionsSignature["CHAGE_OPI"] != $this->hash) {
            return false;
        }
        //実行結果０は、１とする。(とりあえず)
        $ret = $ret == 0 ? 1 : $ret;
        //検証結果
        if ($ret != 1) {
            return false;
        }
        knja120msQuery::getInsUpdSql($db, $this, $seq, $this->hash, $this->signature);

        return true;
    }

    //ハッシュ値を作る
    function makeHash($tRow, $study, $attend) {

        $data = $tRow["TOTALSTUDYACT"].$tRow["TOTALSTUDYVAL"].$tRow["SPECIALACTREMARK"].$tRow["TOTALREMARK"].$tRow["ATTENDREC_REMARK"].
                $study.$attend;

        return sha1($data);
    }
}
?>
