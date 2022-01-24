<?php

require_once('for_php7.php');


class knjd143Model extends Model {
    
    var $field = array();
    var $data  = array();
    var $cmd;
    var $chaircd = "";

    /**
     * メンバ変数の初期化
     */
    function knjd143Model()
    {
    }

    function init()
    {
        if (VARS::request("PROGRAMID")) {
            $this->field = array();
            common::GetControlMaster_Fnc($this->control);
        }
        //学期
        $this->field["SEMESTER"] = VARS::post("SEMESTER");
        if(VARS::get("SEMESTER")) {
            $this->field["SEMESTER"] = VARS::get("SEMESTER");
        }
        //科目コード
        $this->field["SUBCLASSCD"] = VARS::post("SUBCLASSCD");

        //講座コード
        if($this->cmd != "subclasscd" && VARS::post("CHAIRCD")){
            $this->field["CHAIRCD"] = VARS::post("CHAIRCD");
        } else {
            $this->field["CHAIRCD"] = "";
        }
        $this->field["HEADER"] = "on";
        $this->cmd = VARS::request("cmd");

        $this->conduct_contents_moji = 28;
        $this->conduct_contents_gyo = 10;
        $this->conduct_eval_moji = 28;
        $this->conduct_eval_gyo = 10;

//        //置換
//        if ($this->cmd == "replace_update") {
//            $this->replace_data["selectdata"] = VARS::post("selectdata");
//            $this->replace_data["field"] = array();
//
//            $this->replace_data["check"] = array(VARS::post("RCHECK0"),
//                                                 VARS::post("RCHECK1"),
//                                                 VARS::post("RCHECK2"),
//                                                 VARS::post("RCHECK3"),
//                                                 VARS::post("RCHECK4"));
//            $this->replace_data["check_all"] = VARS::post("RCHECK5");
//
//            if (VARS::post("RCHECK0") == "1") $this->replace_data["field"]["CONDUCT_CONTENTS"]     = VARS::post("CONDUCT_CONTENTS");
//            if (VARS::post("RCHECK1") == "1") $this->replace_data["field"]["CONDUCT_EVAL"]    = VARS::post("CONDUCT_EVAL");
//            if (VARS::post("RCHECK2") == "1") $this->replace_data["field"]["GRAD_VALUE"]        = VARS::post("GRAD_VALUE");
//        }

        if(VARS::get("SUBCLASSCD")) {
            $this->subclasscd = VARS::get("SUBCLASSCD");
        }
        if(VARS::get("CHAIRCD")) {
            $this->chaircd = VARS::get("CHAIRCD");
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        //初期化
        $this->fields = array();
        for ($i = 0; $i < get_count($this->data["SCHREGNO"]); $i++) {
            $conductContents    = "CONDUCT_CONTENTS-".$i;
            $conductEval        = "CONDUCT_EVAL-".$i;
            $grad_value         = "GRAD_VALUE-".$i;
            $chk_calc_credit    = "CHK_CALC_CREDIT-".$i;
            $this->fields["CONDUCT_CONTENTS"][]    = VARS::post($conductContents);
            $this->fields["CONDUCT_EVAL"][]   = VARS::post($conductEval);
            $this->fields["GRAD_VALUE"][]       = VARS::post($grad_value);
        }
        
    }

    //入力項目チェック
    function validateInput() {
        for ($i = 0; $i < get_count($this->data["SCHREGNO"]); $i++) {

            if ($this->validate_row_cnt($this->fields["CONDUCT_EVAL"][$i], ($this->conduct_eval_moji * $this->conduct_eval_gyo)) > $this->conduct_eval_gyo) {
                $this->setWarning("MSG915","( 実施・評価は{$this->conduct_eval_gyo}行までです )");
                return false;
            }
            if ($this->validate_row_cnt($this->fields["CONDUCT_CONTENTS"][$i], ($this->conduct_contents_moji * $this->conduct_contents_gyo)) > $this->conduct_contents_gyo) {
                $this->setWarning("MSG915","( 実施の内容は{$this->conduct_contents_gyo}行までです )");
                return false;
            }
        }
        return true;
    }

    function &getUpdateModel()
    {
        if ($this->validateInput()) {
            knjd143Query::update($this);
            $this->setMessage("MSG201");
        }
    }

//    //一括更新
//    function &getReplaceModel()
//    {
//        //生徒が選択されてない場合
//        if (!strlen($this->replace_data["selectdata"])) {
//            $this->setWarning("MSG304","　　　（生徒）");
//            return false;
//        }
//        //最低ひとつでもチェックがついてる場合のみ更新
//        if (!in_array("1",$this->replace_data["check"])) {
//            $this->setWarning("MSG304","　　（チェック）");
//            return false;
//        }
//        //入力チェック
//        if ($this->validate_row_cnt($this->replace_data["field"]["CONDUCT_EVAL"], ($this->conduct_eval_moji * $this->conduct_eval_gyo)) > $this->conduct_eval_gyo) {
//            $this->setWarning("MSG915","(実施・評価は{$this->conduct_eval_gyo}行までです)");
//            return false;
//        }
//        if ($this->validate_row_cnt($this->replace_data["field"]["CONDUCT_CONTENTS"], ($this->conduct_contents_moji * $this->conduct_contents_gyo)) > $this->conduct_contents_gyo) {
//            $this->setWarning("MSG915","(実施の内容は{$this->conduct_contents_gyo}行までです)");
//            return false;
//        }
//        //チェックがついている項目があるテーブルのみを更新
//        if (in_array("1", $this->replace_data["check"])) {
//            knjd143Query::ReplaceUpdate($this);
//        }
//        $this->setMessage("MSG201");
//        //添え字を０から順序良くする
//        $this->replace_data["check"][] = $this->replace_data["check_all"];
//    }

//    /**
//     * 実行モデルを返します
//    */
//    function &getCsvInputModel()
//    {
//        //ファイルの文字列の取得
//        $this->data_arr = array();
//        $this->err_data = array();
//        $this->data_arr = $this->GetFileString();
//
//        //データの整合性チェック
//        if ($this->IsDataMatched($this->data_arr)) {
//            $erraDatas = "";
//            foreach ($this->err_data as $errKey => $errVal) {
//                $erraDatas .= $errKey."行目　".$errVal["MSG"]."\\n";
//            }
//            $this->setMessage("MSG202","処理済件数　：".get_count($this->data_arr)."件\\nエラー件数    ：".get_count($this->err_data)."件\\n".$erraDatas);
//        }
//    }

//    //取込
//    function GetFileString()
//    {
//        $temp_arr = array();
//        if (!is_uploaded_file($this->file["tmp_name"])) {        //HTTP POSTによりアップロードされたファイルかどうかを調べる
//            $this->warning = "ファイル名が不正です。";
//        } else {
//            //教育課程対応
//            if ($this->Properties["useCurriculumcd"] == '1') {
//                $checkdata = array("SCHREGNO"       => array(8,"学籍番号"),
//                                   "CLASSCD"        => array(2,"教科コード"),
//                                   "SCHOOL_KIND"    => array(2,"学校校種"),
//                                   "CURRICULUM_CD"  => array(2,"教育課程コード"),
//                                   "SUBCLASSCD"     => array(6,"科目コード"),
//                                   "CONDUCT_CONTENTS"  => array(678,"学習内容"),
//                                   "CONDUCT_EVAL" => array(500,"評価"),
//                                   "GRAD_VALUE"     => array(2,"学年評定"));
//            } else {
//                $checkdata = array("SCHREGNO"       => array(8,"学籍番号"),
//                                   "SUBCLASSCD"     => array(6,"科目コード"),
//                                   "CONDUCT_CONTENTS"  => array(678,"学習内容"),
//                                   "CONDUCT_EVAL" => array(500,"評価"),
//                                   "GRAD_VALUE"     => array(2,"学年評定"));
//            }
//
//            $db = Query::dbCheckOut();
//            $db->autoCommit(false);
//
//            $fp = fopen ($this->file["tmp_name"], "r");
//            $i  = 0;
//            $line = 1;  //行数
//            $header = false;
//            while (!feof ($fp)) {
//                $buffer = fgets($fp, 4096);
//                if (trim($buffer) != '') {
//                    if (!$header && $this->field["HEADER"] == "on"){
//                        $header = true;
//                        $line++;
//                        continue;
//                    }
//                    //文字コードを判別、改行コードを削除
//                    $buffer = trim(i18n_convert($buffer, i18n_http_output(), "SJIS-win"));
//                    $tmp    = common::csv2array($buffer);
//                    $tmp    = str_replace("↓", "\n", $tmp);
//                    
//                    //教育課程対応
//                    if ($this->Properties["useCurriculumcd"] == '1') {
//                        $data = array("SCHREGNO"        => (strlen(trim($tmp[3]))) ? sprintf("%08d",$tmp[3]) : "",
//                                      "CLASSCD"         => $tmp[4],
//                                      "SCHOOL_KIND"     => $tmp[5],
//                                      "CURRICULUM_CD"   => $tmp[6],
//                                      "SUBCLASSCD"      => $tmp[7],
//                                      "CONDUCT_CONTENTS"   => $tmp[8],
//                                      "CONDUCT_EVAL"  => $tmp[9],
//                                      "GRAD_VALUE"      => $tmp[10]);
//                    } else {
//                        $data = array("SCHREGNO"        => (strlen(trim($tmp[3]))) ? sprintf("%08d",$tmp[3]) : "",
//                                      "SUBCLASSCD"      => $tmp[4],
//                                      "CONDUCT_CONTENTS"   => $tmp[5],
//                                      "CONDUCT_EVAL"  => $tmp[6],
//                                      "GRAD_VALUE"      => $tmp[7]);
//                    }
//
//                    //エラーチェック
//                    if ($this->errCheck($db, $data, $checkdata, $line)) {
//                        $temp_arr[$data["SCHREGNO"]] = $data;
//                        $i++;
//                    }
//                    $line++;
//                }
//            }
//            $db->commit(); // トランザクションをコミットする。
//            Query::dbCheckIn($db);
//            fclose($fp);
//        }
//
//        return $temp_arr;
//    }

//    //エラーチェック
//    function errCheck($db, $data, $data_each, $line)
//    {
//        $returnFlg = true;
//        $hissuErrFlg     = false;    //必須エラーフラグ
//        $numericErrFlg   = false;    //数字エラーフラグ
//        $lengthErrFlg    = false;    //レングスエラーフラグ
//
//        $hissuErrMsg     = "";       //必須エラーメッセージ
//        $numericErrMsg   = "";       //数字エラーメッセージ
//        $lengthErrMsg    = "";       //レングスエラーメッセージ
//
//        $hissuSep     = "";
//        $existenceSep = "";
//        $numericSep   = "";
//        $lengthSep    = "";
//
//        foreach ($data_each as $key => $val) {
//
//            //必須項目チェック
//            //教育課程対応
//            if ($this->Properties["useCurriculumcd"] == '1') {
//                if (in_array($key,array("SCHREGNO", "CLASSCD", "SCHOOL_KIND", "CURRICULUM_CD", "SUBCLASSCD"))) {
//                    if (!strlen(trim($data[$key]))) {
//                        $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
//                    }
//                }
//            } else {
//                if (in_array($key,array("SCHREGNO", "SUBCLASSCD"))) {
//                    if (!strlen(trim($data[$key]))) {
//                        $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
//                    }
//                }
//            }
//            
//            //数値チェック
//            //教育課程対応
//            if ($this->Properties["useCurriculumcd"] == '1') {
//                if (in_array($key,array("SCHREGNO",  "CLASSCD", "CURRICULUM_CD", "SUBCLASSCD", "GRAD_VALUE")) {
//                    if ($data[$key] != "" && !is_numeric($data[$key])) {
//                        $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
//                    }
//                }
//            } else {
//                if (in_array($key,array("SCHREGNO", "SUBCLASSCD", "GRAD_VALUE")) {
//                    if ($data[$key] != "" && !is_numeric($data[$key])) {
//                        $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
//                    }
//                }
//            }
//        }
//
//        if (!$returnFlg) {
//            $check_error = $hissuErrMsg.$numericErrMsg.$lengthErrMsg;
//            if (strlen($check_error) > 120) {
//                $check_error = mb_strcut($check_error,0,115);
//                $check_error .= "..他";
//            }
//            $this->err_data[$line]["MSG"] = $check_error;
//        }
//        return $returnFlg;
//    }

//    //エラーメッセージをセット
//    function errSet(&$errFlg, &$errMsg, $firstMsg, &$sep, $val)
//    {
//        if (!$errFlg) {
//            $errMsg .= $firstMsg;
//            $errFlg = true;
//        }
//        $errMsg .= $sep.$val;
//        $sep = "/";
//        return false;
//    }

//    //データの整合性チェック
//    function IsDataMatched(&$data_arr)
//    {
//        //５千件を超えたら制限エラー
//        if (get_count($data_arr) > 5000) {
//            $this->setWarning("MSG915","処理件数を５千件以下にして下さい 処理件数：".get_count($data_arr)."件");
//            return false;
//        }
//        return true;
//
//    }


//    //ＣＳＶ出力
//    function getCsvOutputModel()
//    {
//        //項目
//        //教育課程対応
//        if ($this->Properties["useCurriculumcd"] == '1') {
//            $header = array("HR_NAME"           => "クラス",
//                            "ATTENDNO"          => "出席番号",
//                            "NAME_SHOW"         => "氏名",
//                            "SCHREGNO"          => "※学籍番号",
//                            "CLASSCD"           => "※教科コード",
//                            "SCHOOL_KIND"       => "※学校校種",
//                            "CURRICULUM_CD"     => "※教育課程コード",
//                            "SUBCLASSCD"        => "※科目コード",
//                            "CONDUCT_CONTENTS"     => "学習内容",
//                            "CONDUCT_EVAL"    => "評価",
//                            "GRAD_VALUE"        => "学年評定",
//                            "DUMMY"             => "DUMMY");
//        } else {
//            $header = array("HR_NAME"           => "クラス",
//                            "ATTENDNO"          => "出席番号",
//                            "NAME_SHOW"         => "氏名",
//                            "SCHREGNO"          => "※学籍番号",
//                            "SUBCLASSCD"        => "※科目コード",
//                            "CONDUCT_CONTENTS"     => "学習内容",
//                            "CONDUCT_EVAL"    => "評価",
//                            "GRAD_VALUE"        => "学年評定",
//                            "DUMMY"             => "DUMMY");
//        }
//
//        $contents  = "";    //クリア
//
//         $contents .= implode($header, ",")."\n";
//
//        //DB接続
//        $db = Query::dbCheckOut();
//
//        //データ
//        $sdate = str_replace("/","-",$this->control["学期開始日付"][CTRL_SEMESTER]);
//        $edate = str_replace("/","-",$this->control["学期終了日付"][CTRL_SEMESTER]);
//        if ($sdate <= CTRL_DATE && CTRL_DATE <= $edate) {
//            $execute_date = CTRL_DATE;//初期値
//        } else {
//            $execute_date = $edate;//初期値
//        }
//        $query = knjd143Query::selectQuery($this, $execute_date);
//        //SUBCLASSCDセット
//        //教育課程対応
//        if ($this->Properties["useCurriculumcd"] == '1') {
//            $substrcd = array();
//            $substrcd = explode("-",$this->field["SUBCLASSCD"]);
//        }
//
//        $header_data = false;
//
//        //SQL文発行
//        $result = $db->query($query);
//        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
//            $row = str_replace(","   , "." , $row);  //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
//            $row = str_replace("\r\n", "↓", $row);
//            $row = str_replace("\r"  , "↓", $row);
//            $row = str_replace("\n"  , "↓", $row);
//            $setData = array();
//            $setData["HR_NAME"] = $row["HR_NAME"];
//            $setData["ATTENDNO"] = $row["ATTENDNO"];
//            $setData["NAME_SHOW"] = $row["NAME_SHOW"];
//            $setData["SCHREGNO"] = $row["SCHREGNO"];
//            //教育課程対応
//            if ($this->Properties["useCurriculumcd"] == '1') {
//                if ($row["CLASSCD"]) {
//                    $setData["CLASSCD"]         = $row["CLASSCD"];
//                } else {
//                    $setData["CLASSCD"]         = $substrcd[0];
//                }
//                if ($row["SCHOOL_KIND"]) {
//                    $setData["SCHOOL_KIND"]     = $row["SCHOOL_KIND"];
//                } else {
//                    $setData["SCHOOL_KIND"]     = $substrcd[1];
//                }
//                if ($row["CURRICULUM_CD"]) {
//                    $setData["CURRICULUM_CD"]   = $row["CURRICULUM_CD"];
//                } else {
//                    $setData["CURRICULUM_CD"]   = $substrcd[2];
//                }
//                if ($row["SUBCLASSCD"]) {
//                    $setData["SUBCLASSCD"]   = $row["SUBCLASSCD"];
//                } else {
//                    $setData["SUBCLASSCD"]   = $substrcd[3];
//                }
//            } else {
//                if ($row["SUBCLASSCD"]) {
//                    $setData["SUBCLASSCD"] = $row["SUBCLASSCD"];
//                } else {
//                    $setData["SUBCLASSCD"]   = $this->field["SUBCLASSCD"];
//                }
//            }
//            $setData["CONDUCT_CONTENTS"] = $row["CONDUCT_CONTENTS"];
//            $setData["CONDUCT_EVAL"] = $row["CONDUCT_EVAL"];
//            $setData["GRAD_VALUE"] = $row["GRAD_VALUE"];
//            $setData["DUMMY"] = "";
//            $contents .= implode($setData, ",") ."\n";
//            $header_data = true;
//        }
//        $result->free();
//        Query::dbCheckIn($db);
//
//        //ＣＳＶ出力
//        if ($header_data) {
//            $couse = "成績入力（総合的な学習の時間）";
//
//            $filename = sprintf("%s.csv",$couse);
//            $filename = i18n_convert($filename,"SJIS-win");
//            $contents = i18n_convert($contents,"SJIS-win");
//            /* HTTPヘッダの出力 */
//            i18n_http_output("pass");
//            header("Accept-Ranges: none");
//            header("Content-Disposition: inline; filename=$filename");
//            header("Content-Transfer-Encoding: binary");
//            header("Content-Length: ". strlen($contents) );
//            header("Content-Type: text/octet-stream");
//
//            echo $contents;
//            return true;
//        } else {
//            $this->warning = "該当データは、ありません。";
//            return false;
//        }
//
//    }

}
?>
