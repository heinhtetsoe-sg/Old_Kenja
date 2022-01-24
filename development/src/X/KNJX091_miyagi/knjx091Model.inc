<?php

require_once('for_php7.php');

class knjx091Model extends Model {
    var $field      = array();
    var $data       = array();
    var $cmd;
    var $file;
    var $control; //コントロールマスタ
    var $staffcd; //登録者コード
    var $fieldSize = array();

    function init() {
        common::GetControlMaster_Fnc($this->control);

        $this->field = array("OUTPUT"        =>  VARS::post("OUTPUT"),        //出力取込種別
                             "YEAR_SEMESTER" =>  VARS::post("YEAR_SEMESTER"), //年度_学期
                             "GRADE"         =>  VARS::post("GRADE"),         //学年
                             "SUBCLASS"      =>  VARS::post("SUBCLASS"),      //科目
                             "HEADER"        =>  VARS::post("HEADER"),        //ヘッダ有
                             "SHORI_MEI"     =>  VARS::post("SHORI_MEI")      //処理名
                             );

        $this->cmd = VARS::request("cmd");

        if (VARS::request("cmd") != "") {
            $this->field["YEAR"]     = substr(VARS::post("YEAR_SEMESTER"), 0, 4);
            $this->field["SEMESTER"] = substr(VARS::post("YEAR_SEMESTER"), 4);
        } else {
            $this->field["YEAR"]     = CTRL_YEAR;
            $this->field["SEMESTER"] = CTRL_SEMESTER;
        }

        if (VARS::file("FILE")){
            $this->field["FILE"] = VARS::file("FILE");        //ファイルのアップロード
            $this->field["FILE"]["name"] = mb_convert_encoding($this->field["FILE"]["name"], "SJIS-win", "UTF-8");
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();
    }

    /***************/
    /* CSVの取込み */
    /***************/
    function &getExecModel() {
        //ファイルの文字列の取得
        $this->data_arr = array();
        $this->data_arr = $this->GetFileString();

        //データの整合性チェック
        if ( $this->IsDataMatched($this->data_arr) ) {
            if ($this->field["SHORI_MEI"] == "1") {
                $cnt = knjx091Query::DeleteQueryCsv($this,$this->data_arr); //処理件数を返すけれども使わない。とりあえず代入しただけ。
                $cnt = knjx091Query::insertQueryCsv($this,$this->data_arr);
            } else {
                $cnt = knjx091Query::DeleteQueryCsv($this,$this->data_arr);
            }
            $db = Query::dbCheckOut();
            $errcnt = $db->getOne("SELECT COUNT(*) FROM W_CSVMSG_DAT");
            Query::dbCheckIn($db);

            $this->setMessage("MSG202","処理済件数　：".$cnt."件\\nエラー件数    ：".$errcnt."件");
        }
    }

    /***********************/
    /* CSVファイルを配列へ */
    /***********************/
    function GetFileString() {
        $temp_arr = array();
        if (!is_uploaded_file($this->field["FILE"]["tmp_name"])) { //HTTP POSTによりアップロードされたファイルかどうかを調べる
            $this->warning = "ファイル名が不正です。";
        } else {
            if ($this->Properties["useCurriculumcd"] == '1') {
                $checkdata = array("YEAR"          => array(4,"年度"),
                                   "SEMESTER"      => array(1,"学期"),
                                   "GROUPCD"       => array(3,"選択グループコード"),
                                   "CLASSCD"       => array(2,"教科コード"),
                                   "SCHOOL_KIND"   => array(1,"学校種別"),
                                   "CURRICULUM_CD" => array(1,"教育課程コード"),
                                   "SUBCLASSCD"    => array(6,"科目コード"),
                                   "GRADE"         => array(2,"学年"),
                                   "SCHREGNO"      => array(8,"学籍番号"));
            } else {
                $checkdata = array("YEAR"          => array(4,"年度"),
                                   "SEMESTER"      => array(1,"学期"),
                                   "GROUPCD"       => array(3,"選択グループコード"),
                                   "CLASSCD"       => array(2,"教科コード"),
                                   "CURRICULUM_CD" => array(1,"教育課程コード"),
                                   "SUBCLASSCD"    => array(6,"科目コード"),
                                   "GRADE"         => array(2,"学年"),
                                   "SCHREGNO"      => array(8,"学籍番号"));
            }

            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            //エラーデータの削除
            $query = knjx091Query::deleteQueryErr();
            $result = $db->query($query);

            $fp = fopen ($this->field["FILE"]["tmp_name"], "r");
            $i  = 0;
            $line = 1;  //行数
            $header = false;
            while (!feof ($fp)) {
                $buffer = fgets($fp, 4096);
                if (trim($buffer) != '') {
                    if (!$header && $this->field["HEADER"] == "on"){
                        $header = true;
                        $line++;
                        continue;
                    }
                    //文字コードを判別、改行コードを削除
                    $buffer = trim(mb_convert_encoding($buffer, "SJIS-win", mb_http_output()));
                    $tmp    = common::csv2array($buffer);

                    if ($this->Properties["useCurriculumcd"] == '1') {
                        $data = array("YEAR"           => (strlen(trim($tmp[0])))  ? sprintf("%04d",$tmp[0])  : "",
                                      "SEMESTER"       => (strlen(trim($tmp[1])))  ? sprintf("%01d",$tmp[1])  : "",
                                      "GROUPCD"        => (strlen(trim($tmp[2])))  ? sprintf("%03d",$tmp[2])  : "",
                                      "CLASSCD"        => (strlen(trim($tmp[3])))  ? sprintf("%02d",$tmp[3])  : "",
                                      "SCHOOL_KIND"    => (strlen(trim($tmp[4])))  ? $tmp[4]  : "",
                                      "CURRICULUM_CD"  => (strlen(trim($tmp[5])))  ? sprintf("%01d",$tmp[5])  : "",
                                      "SUBCLASSCD"     => (strlen(trim($tmp[6])))  ? sprintf("%06d",$tmp[6])  : "",
                                      "GRADE"          => (strlen(trim($tmp[7])))  ? sprintf("%02d",$tmp[7])  : "",
                                      "SCHREGNO"       => (strlen(trim($tmp[11]))) ? sprintf("%08d",$tmp[11]) : "");
                    } else {
                        $data = array("YEAR"           => (strlen(trim($tmp[0])))  ? sprintf("%04d",$tmp[0])  : "",
                                      "SEMESTER"       => (strlen(trim($tmp[1])))  ? sprintf("%01d",$tmp[1])  : "",
                                      "GROUPCD"        => (strlen(trim($tmp[2])))  ? sprintf("%03d",$tmp[2])  : "",
                                      "CLASSCD"        => (strlen(trim($tmp[3])))  ? sprintf("%02d",$tmp[3])  : "",
                                      "CURRICULUM_CD"  => (strlen(trim($tmp[4])))  ? sprintf("%01d",$tmp[4])  : "",
                                      "SUBCLASSCD"     => (strlen(trim($tmp[5])))  ? sprintf("%06d",$tmp[5])  : "",
                                      "GRADE"          => (strlen(trim($tmp[6])))  ? sprintf("%02d",$tmp[6])  : "",
                                      "SCHREGNO"       => (strlen(trim($tmp[10]))) ? sprintf("%08d",$tmp[10]) : "");
                    }

                    //エラーチェック
                    if ($this->errCheck($db, $data, $checkdata, $line)) {
                        $temp_arr[$i] = $data;
                        $i++;
                    }
                    $line++;
                }
            }
            $db->commit(); // トランザクションをコミットする。
            Query::dbCheckIn($db);
            fclose($fp);
        }

        return $temp_arr;
    }

    
    /*****************/
    /*エラーチェック */
    /*****************/
    function errCheck($db, $data, $checkdata, $line) {
        $returnFlg = true;
        $hissuErrFlg     = false;    //必須エラーフラグ
        $existenceErrFlg = false;    //存在エラーフラグ
        $numericErrFlg   = false;    //数字エラーフラグ
        $lengthErrFlg    = false;    //レングスエラーフラグ

        $hissuErrMsg     = "";       //必須エラーメッセージ
        $existenceErrMsg = "";       //存在エラーメッセージ
        $numericErrMsg   = "";       //数字エラーメッセージ
        $lengthErrMsg    = "";       //レングスエラーメッセージ

        $hissuSep     = "";
        $existenceSep = "";
        $existenceSep = "";
        $numericSep   = "";
        $lengthSep    = "";

        //学校種別
        $school_kind = $db->getCol(knjx091Query::getSchoolKind());
        //教育課程
        $curriculum_cd = $db->getCol(knjx091Query::getCurriculumCd());

        foreach ($checkdata as $key => $val) {
            if ($this->Properties["useCurriculumcd"] == '1') {
                //必須項目チェック
                if (in_array($key,array("YEAR","SEMESTER","GROUPCD","CLASSCD","SCHOOL_KIND","CURRICULUM_CD","SUBCLASSCD","GRADE","SCHREGNO"))) {
                    if (!strlen(trim($data[$key]))) {
                        $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                    } else {
                        if ($this->field["SHORI_MEI"] == "1") {
                            if ($key == 'SCHOOL_KIND' && !in_array($data[$key],$school_kind)) {
                                $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[1]);
                            }
                            if ($key == 'CURRICULUM_CD' && !in_array($data[$key],$curriculum_cd)) {
                                $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[1]);
                            }
                        }
                    }
                }
            } else {
                //必須項目チェック
                if (in_array($key,array("YEAR","SEMESTER","GROUPCD","CLASSCD","CURRICULUM_CD","SUBCLASSCD","GRADE","SCHREGNO"))) {
                    if (!strlen(trim($data[$key]))) {
                        $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                    } else {
                        if ($this->field["SHORI_MEI"] == "1") {
                            if ($key == 'CLASSCD' && 0 == $db->getOne(knjx091Query::classcdCntSql($this, $data, $data[$key]))) {
                                $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[1]);
                            }
                            if ($key == 'CURRICULUM_CD' && 2 != $data[$key]) {
                                $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[1]);
                            }
                            if ($key == 'SUBCLASSCD' && 0 == $db->getOne(knjx091Query::subclassCntSql($this, $data, $data[$key]))) {
                                $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[1]);
                            }
                        }
                    }
                }
            }

            //数値チェック
            if (in_array($key,array("YEAR","SEMESTER","GROUPCD","CLASSCD","CURRICULUM_CD","SUBCLASSCD","SCHREGNO"))) {
                if ($data[$key] != "" && !is_numeric($data[$key])) {
                    $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                }
            }

            if ($this->field["SHORI_MEI"] == "1") {
                //文字数チェック
                if (strlen($data[$key]) > $val[0]) {
                    $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                }
            }
        }

        //マスタの存在チェック
        if ($this->Properties["useCurriculumcd"] == '1') {
            if ($this->field["SHORI_MEI"] == "1") {
                if (0 == $db->getOne(knjx091Query::classcdCntSql($this, $data, $data["CLASSCD"]))) {
                    $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, "教科コード");
                }
                if (0 == $db->getOne(knjx091Query::subclassCntSql($this, $data, $data["SUBCLASSCD"]))) {
                    $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, "科目コード");
                }
            }
        }

        //YEAR、SEMESTER、SCHREGNO の入力があった場合
        //SCHREG_REGD_DAT にその生徒が登録されているのかチェックする
        if (strlen(trim($data["YEAR"])) && strlen(trim($data["SEMESTER"])) && strlen(trim($data["SCHREGNO"]))) {
            if (0 == $db->getOne(knjx091Query::schregNoCntSql($data["YEAR"], $data["SEMESTER"], $data["SCHREGNO"]))) {
                $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, "学籍番号");
            }
        }

        //SUBCLASS_COMP_SELECT_DATの存在チェック
        if ($this->Properties["useCurriculumcd"] == '1') {
            if (strlen(trim($data["YEAR"])) && strlen(trim($data["GRADE"])) && strlen(trim($data["GROUPCD"])) && strlen(trim($data["CLASSCD"])) && strlen(trim($data["SUBCLASSCD"])) && strlen(trim($data["SCHOOL_KIND"]))) {
                if (0 == $db->getOne(knjx091Query::groupcdCntSql($data["YEAR"], $data["GRADE"], $data["GROUPCD"], $data["CLASSCD"], $data["SUBCLASSCD"], $data["SCHOOL_KIND"]))) {
                    $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, "選択グループコード");
                }
            }
        } else {
            if (strlen(trim($data["YEAR"])) && strlen(trim($data["GRADE"])) && strlen(trim($data["GROUPCD"])) && strlen(trim($data["CLASSCD"])) && strlen(trim($data["SUBCLASSCD"]))) {
                if (0 == $db->getOne(knjx091Query::groupcdCntSql($data["YEAR"], $data["GRADE"], $data["GROUPCD"], $data["CLASSCD"], $data["SUBCLASSCD"]))) {
                    $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, "選択グループコード");
                }
            }
        }

        if (!$returnFlg) {
            $check_error = $hissuErrMsg.$numericErrMsg.$lengthErrMsg.$existenceErrMsg;
            if (strlen($check_error) > 120) {
                $check_error = mb_strcut($check_error,0,115);
                $check_error .= "..他";
            }
            knjx091Query::insertQueryErr($db, $line, $check_error);
        }
        return $returnFlg;
    }

    /****************************/
    /* エラーメッセージをセット */
    /****************************/
    function errSet(&$errFlg, &$errMsg, $firstMsg, &$sep, $val) {
        if (!$errFlg) {
            $errMsg .= $firstMsg;
            $errFlg = true;
        }
        $errMsg .= $sep.$val;
        $sep = "/";
        return false;
    }

    /**************************/
    /* データの整合性チェック */
    /**************************/
    function IsDataMatched(&$data_arr) {
        //５千件を超えたら制限エラー
        if (get_count($data_arr) > 5000) {
            $this->setWarning("MSG915","処理件数を５千件以下にして下さい 処理件数：".get_count($data_arr)."件");
            return false;
        }
        return true;

    }

    /**************/
    /* ＣＳＶ出力 */
    /**************/
    function getDownloadModel() {
        //項目
        $header_err = array("MSGROW"        => "行番号",
                            "MSGREMARK"     => "メッセージ");

        if ($this->Properties["useCurriculumcd"] == '1') {
            $header     = array("YEAR"          => "※年度",
                                "SEMESTER"      => "※学期",
                                "GROUPCD"       => "※選択グループコード",
                                "CLASSCD"       => "※教科コード",
                                "SCHOOL_KIND"   => "※学校種別",
                                "CURRICULUM_CD" => "※教育課程コード",
                                "SUBCLASSCD"    => "※科目コード",
                                "GRADE"         => "※学年",
                                "HR_CLASS"      => "組",
                                "ATTENDNO"      => "出席番号",
                                "NAME"          => "氏名",
                                "SCHREGNO"      => "※学籍番号",
                                "DUMMY"         => "DUMMY");

            $db = Query::dbCheckOut();
            //学校種別ヘッダ例取得
            $school_kind_rei = "";
            $result = $db->query(knjx091Query::getSchoolKind());
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                if ($school_kind_rei != "") $school_kind_rei .= " ";
                $school_kind_rei .= $row["NAME1"].":".$row["ABBV1"];
            }
            $result->free();
            if ($school_kind_rei == "") $school_kind_rei = "名称マスタ未設定";
            Query::dbCheckIn($db);

            $header_rei = array("YEAR"          => CTRL_YEAR,
                                "SEMESTER"      => CTRL_SEMESTER,
                                "GROUPCD"       => "001",
                                "CLASSCD"       => "99",
                                "SCHOOL_KIND"   => $school_kind_rei,
                                "CURRICULUM_CD" => "2",
                                "SUBCLASSCD"    => "999999",
                                "GRADE"         => "01",
                                "HR_CLASS"      => "001",
                                "ATTENDNO"      => "001",
                                "NAME"          => "アルプ太郎",
                                "SCHREGNO"      => "12345678",
                                "DUMMY"         => "DUMMY");
        } else {
            $header     = array("YEAR"          => "※年度",
                                "SEMESTER"      => "※学期",
                                "GROUPCD"       => "※選択グループコード",
                                "CLASSCD"       => "※教科コード",
                                "CURRICULUM_CD" => "※教育課程コード",
                                "SUBCLASSCD"    => "※科目コード",
                                "GRADE"         => "※学年",
                                "HR_CLASS"      => "組",
                                "ATTENDNO"      => "出席番号",
                                "NAME"          => "氏名",
                                "SCHREGNO"      => "※学籍番号",
                                "DUMMY"         => "DUMMY");

            $header_rei = array("YEAR"          => CTRL_YEAR,
                                "SEMESTER"      => CTRL_SEMESTER,
                                "GROUPCD"       => "001",
                                "CLASSCD"       => "99",
                                "CURRICULUM_CD" => "2",
                                "SUBCLASSCD"    => "999999",
                                "GRADE"         => "01",
                                "HR_CLASS"      => "001",
                                "ATTENDNO"      => "001",
                                "NAME"          => "アルプ太郎",
                                "SCHREGNO"      => "12345678",
                                "DUMMY"         => "DUMMY");
        }

        $contents  = "";    //クリア

        $header_data = false;   //出力フラグ


        if ($this->field["OUTPUT"]=="1") {
            // ヘッダ出力
            $header_data = true;
            $contents .= implode($header, ",")."\n";
            $contents .= implode($header_rei, ",")."\n";
        } else if ($this->field["OUTPUT"]=="3") {
            // エラー出力
            $contents .= implode($header_err, ",")."\n";
        } else if ($this->field["OUTPUT"]=="4") {
            // データ出力
            if( $this->field["HEADER"]=="on" ) $contents .= implode($header, ",")."\n";
        }

        //ヘッダ以外
        if ($this->field["OUTPUT"] != "1") {
            $db = Query::dbCheckOut();

            if($this->field["OUTPUT"]=="3") {
                //エラー
                $query = knjx091Query::selectCsvErrQuery();
            } else if ($this->field["OUTPUT"]=="4") {
                //データ
                $query = knjx091Query::selectMainQuery($this);
            }

            //SQL文発行
            $result = $db->query($query);
            while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                if($this->field["OUTPUT"]=="4"){                //データ
                    $row = str_replace("\r\n","",str_replace(",",".",$row));   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                }
                $contents .= implode($row, ",") ."\n";
                $header_data = true;
            }

            $result->free();
            Query::dbCheckIn($db);
        }

        //ＣＳＶ出力
        if ($header_data) {
            if($this->field["OUTPUT"]=="3"){        //エラー
                $couse = "エラー出力";
            } else {                                //エラー以外
                $couse = "履修登録データ";
            }
            $filename = sprintf("%s.csv",$couse);
            $filename = i18n_convert($filename,"SJIS-win");
            $contents = i18n_convert($contents,"SJIS-win");
            /* HTTPヘッダの出力 */
            i18n_http_output("pass");
            header("Accept-Ranges: none");
            header("Content-Disposition: inline; filename=$filename");
            header("Content-Transfer-Encoding: binary");
            header("Content-Length: ". strlen($contents) );
            header("Content-Type: text/octet-stream");

            echo $contents;
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }
}
?>
