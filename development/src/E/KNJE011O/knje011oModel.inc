<?php

require_once('for_php7.php');

class knje011oModel extends Model {
    var $cmd;
    var $schregno;      //学籍番号
    var $field;         //画面上のデータ
    var $field2;        //画面上のデータ
    var $grade;         //学年
    var $annual;        //学年(年次)
    var $control;
    var $mode;          //在籍卒業モード
    var $grd_year;      //卒業年度
    var $grd_semester;  //卒業学期
    var $exp_year;      //左フレームから送られてくる年度
    var $exp_semester;  //左フレームから送られてくる学期

    var $schoolCd;
    var $getPrgId;  //コールされたか
    var $sendAuth;  //親画面の権限
    var $auth;      //権限

    function knje011oModel() {
        $this->year = CTRL_YEAR;
        $this->mode = "ungrd";      //在籍モード
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {

        if (VARS::get("PROGRAMID")) {
            unset($this->getPrgId);
            unset($this->sendAuth);
            unset($this->auth);
        }
        if(VARS::get("SEND_PRGID")){
            $this->getPrgId = VARS::get("SEND_PRGID");
            $this->sendAuth = VARS::get("SEND_AUTH");
        }
        $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;

        $this->getPropertiesAll(); //プロパティファイル

        $this->cmd     = VARS::request("cmd");
        //次の生徒
        $this->nextURL = VARS::post("nextURL");

        //左フレームから送られてくる年度
        if (VARS::get("EXP_YEAR")) {
            $this->exp_year = VARS::get("EXP_YEAR");
        }
        //左フレームから送られてくる学期
        if (VARS::get("EXP_SEMESTER")) {
            $this->exp_semester = VARS::get("EXP_SEMESTER");
        }

        //表示科目
        if (VARS::get("SCHREGNO")) {
            $this->schregno = VARS::get("SCHREGNO"); //学籍番号
        }
        if (VARS::get("NAME")) {
            $this->name = VARS::get("NAME");         //生徒氏名
        }
        if (VARS::get("GRADE")) {
            $this->grade = VARS::get("GRADE");       //学年
        }

        //在籍卒業モード
        if (VARS::get("mode")) {
            $this->mode = VARS::get("mode");         //在籍卒業モード
        }
        if (VARS::get("GRD_YEAR")) {
            $this->grd_year = VARS::get("GRD_YEAR"); //卒業年度
        }
        if (VARS::get("GRD_SEMESTER")) {
            $this->grd_semester = VARS::get("GRD_SEMESTER"); //卒業学期
        }

        $this->field = array("DISEASE"        => VARS::post("DISEASE"),            //疾患および異常
                             "DOC_REMARK"     => VARS::post("DOC_REMARK"),         //担当学校医所見
                             "TR_REMARK"      => VARS::post("TR_REMARK"),          //担任所見
                             "TOTALSTUDYACT"  => VARS::post("TOTALSTUDYACT"),      //活動内容
                             "TOTALSTUDYVAL"  => VARS::post("TOTALSTUDYVAL"),      //評価
                             "REMARK"         => VARS::post("REMARK"),             //備考
                             "COMMENTEX_A_CD" => VARS::post("COMMENTEX_A_CD")      //学習成績概評
                             );

        $this->field2 = array("SPECIALACTREC"    => VARS::post("SPECIALACTREC"),   //特別活動の記録
                              "TRAIN_REF"        => VARS::post("TRAIN_REF"),       //指導上参考になる諸事項1
                              "TRAIN_REF2"       => VARS::post("TRAIN_REF2"),     //指導上参考になる諸事項2
                              "TRAIN_REF3"       => VARS::post("TRAIN_REF3"),     //指導上参考になる諸事項3
                              "ATTENDREC_REMARK" => VARS::post("ATTENDREC_REMARK") //出欠の記録備考
                              );
        //学年（年次）
        if (VARS::post("ANNUAL")) {
            list($tmp["YEAR"], $tmp["ANNUAL"]) = explode(",", VARS::post("ANNUAL"));
            $this->annual = $tmp;
        }

        //生徒一覧を更新
        if (VARS::get("init")) {
            unset($this->schregno); //学籍番号
            unset($this->name);     //生徒氏名
        }

        $this->getProperties();
    }

    function &getProgrampathModel()
    {
        $db = Query::dbCheckOut();
        $programpath = $db->getOne(knje011oQuery::getProgrampath());
        Query::dbCheckIn($db);

        return $programpath;
    }

    function getProperties() {
        $retVal = "";
    
        /*
         * configディレクトリ確認
         */
        if (file_exists(CONFDIR ."/prgInfo.properties")) {
            $filename = CONFDIR ."/prgInfo.properties";
        } else {
            $filename = DOCUMENTROOT ."/prgInfo.properties";
        }

        $fp = @fopen($filename, 'r');
        if ($fp) {
            while ($line = fgets($fp,1024)) {
                $pos = strpos($line, "useSyojikou3");
                // === を使用していることに注目しましょう。単純に == を使ったのでは
                // 期待通りに動作しません。なぜなら 'doRun' が 0 番目 (最初) の文字だからです。
                if ($pos === false) {
                    $this->useSyojikou3 = "0";
                } else {
                    $retVal = str_replace("useSyojikou3 = ", "", $line);
                    if (str_replace("\r\n", "", $retVal) == "1") {
                        $this->useSyojikou3 = str_replace("\r\n", "", $retVal);
                        break;
                    } else {
                        $this->useSyojikou3 = "0";
                    }
                }
            }
            fclose($fp);
        } else {
            $this->useSyojikou3 = "0";
        }

        return $retVal;
    }

    //入力項目チェック
    function validateInput() {
        if (!isset($this->schregno)) return false;

        if (strlen($this->field["TOTALSTUDYACT"]) > 248) {
            $this->setWarning("MSG914", "( 総合学習...活動内容 ) 248バイトまで");
            return false;
        } elseif (strlen($this->field["TOTALSTUDYVAL"]) > 373) {
            $this->setWarning("MSG914", "( 総合学習...評価 ) 373バイトまで");
            return false;
        } elseif (strlen($this->field["REMARK"]) > 623) {
            $this->setWarning("MSG914", "( 備考 ) 623バイトまで");
            return false;
        } elseif (strlen($this->field2["ATTENDREC_REMARK"]) > 49) {
            $this->setWarning("MSG914", "( 出欠の記録備考 ) 49バイトまで");
            return false;
        } elseif (strlen($this->field2["SPECIALACTREC"]) > 278) {
            $this->setWarning("MSG914", "( 特別活動の記録 ) 278バイトまで");
            return false;
        } elseif ($this->useSyojikou3 == "" && strlen($this->field2["TRAIN_REF"]) > 623) {
            $this->setWarning("MSG914", "( 指導上参考になる諸事項 ) 623バイトまで");
            return false;
        } elseif ($this->useSyojikou3 == "1" && strlen($this->field2["TRAIN_REF"]) > 218) {
            $this->setWarning("MSG914", "( 指導上参考になる諸事項1 ) 218バイトまで");
            return false;
        } elseif ($this->useSyojikou3 == "1" && strlen($this->field2["TRAIN_REF2"]) > 218) {
            $this->setWarning("MSG914", "( 指導上参考になる諸事項2 ) 218バイトまで");
            return false;
        } elseif ($this->useSyojikou3 == "1" && strlen($this->field2["TRAIN_REF3"]) > 218) {
            $this->setWarning("MSG914", "( 指導上参考になる諸事項3 ) 218バイトまで");
            return false;
        }
        return true;
    }

    //編集完了用のモデルを返します
    function &getUpdateModel() {
        if ($this->validateInput()) {
            $db = Query::dbCheckOut();
            $db->autoCommit(false);
            if ($this->cmd == "update") {
                //更新処理
                knje011oQuery::update($db, $this);
            }else if ($this->cmd == "update2") {
                //更新処理
                knje011oQuery::update2($db, $this);
            }
            $db->commit(); // トランザクションをコミットする。
            Query::dbCheckIn($db);
            $this->setMessage("MSG201");
        }
    }

    //保健より読込みのモデルを返します
    function &getReloadHealthModel() {
        $db = Query::dbCheckOut();

        $query = knje011oQuery::selectQueryHealth($this);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        if (is_array($row)) {
            echo "<script language=\"javascript\">\n";
            if ($row["OTHERDISEASECD"] != "") {
                echo "top.main_frame.right_frame.document.forms[0].DISEASE.value = '" .addslashes($row["OTHERDISEASECD"]) ."';\n";
            }
            if ($row["DOC_REMARK"] != "") {
                echo "top.main_frame.right_frame.document.forms[0].DOC_REMARK.value = '" .addslashes($row["DOC_REMARK"]) ."';\n";
            }
            echo "</script>\n";
        }

        Query::dbCheckIn($db);
    }
}
?>
