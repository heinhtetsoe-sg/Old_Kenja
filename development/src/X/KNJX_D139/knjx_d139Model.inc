<?php

require_once('for_php7.php');

/**
 * 定数定義
*/
class knjx_d139Model extends Model {
    var $field      = array();
    var $data       = array();
    var $cmd;
    var $file;
    var $control;           //コントロールマスタ
    var $staffcd;           //登録者コード

    /**
     * メンバ変数の初期化
     */
    function knjx_d139Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;                            //登録者コード
    }
    function init()
    {

        $this->field = array("SCHOOL_KIND"      =>  VARS::post("SCHOOL_KIND"),      //校種
                             "OUTPUT"           =>  VARS::post("OUTPUT"),           //出力取込種別
                             "YEAR"             =>  VARS::post("YEAR"),             //年度
                             "SEMESTER"         =>  VARS::post("SEMESTER"),         //学期
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //年組
                             "SUBCLASSCD"       =>  VARS::post("SUBCLASSCD"),       //科目
                             "CLASSCD"          =>  VARS::post("CLASSCD"),          //教科
                             "HEADER"           =>  VARS::post("HEADER"),           //ヘッダ有
                             "SHORI_MEI"        =>  VARS::post("SHORI_MEI"),        //処理名
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                             );

        $this->cmd          = VARS::request("cmd");
        
        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
        
        $this->file = VARS::file("FILE");
        
        if (VARS::get("SEND_PRGID")) {
            $this->field["SCHOOL_KIND"]     = (VARS::get("SEND_SCHOOL_KIND")) ? VARS::get("SEND_SCHOOL_KIND") : $this->field["SCHOOL_KIND"];
            $this->field["SEMESTER"]        = (VARS::get("SEND_SEMESTER")) ? VARS::get("SEND_SEMESTER") : $this->field["SEMESTER"];
            $this->field["GRADE_HR_CLASS"]  = (VARS::get("SEND_GRADE_HR_CLASS")) ? VARS::get("SEND_GRADE_HR_CLASS") : $this->field["GRADE_HR_CLASS"];
            $this->field["CLASSCD"]         = (VARS::get("SEND_CLASSCD")) ? VARS::get("SEND_CLASSCD") : $this->field["CLASSCD"];
            $this->field["SUBCLASSCD"]      = (VARS::get("SEND_SUBCLASSCD")) ? VARS::get("SEND_SUBCLASSCD") : $this->field["SUBCLASSCD"];
        }

    }
    function &getMainModel()
    {
    }

    /**
     * 実行モデルを返します
    */
    function &getExecModel()
    {
        //ファイルの文字列の取得
        $this->data_arr = array();
        $this->data_arr = $this->GetFileString();

        //データの整合性チェック
        if ( $this->IsDataMatched($this->data_arr) ) {
            $errFlg = $this->checkCsvErrQuery(PROGRAMID);
            if ($errFlg) {
                if ($this->field["SHORI_MEI"] == "1") {
                    knjx_d139Query::insertQueryCsv($this, $this->data_arr);
                } else {
                    knjx_d139Query::DeleteQueryCsv($this, $this->data_arr);
                }
                $this->setMessage("MSG202", "更新件数".get_count($this->data_arr)."件");
            }
        }
    }

    //取込
    function GetFileString()
    {
        $temp_arr = array();
        if (!is_uploaded_file($this->file["tmp_name"])) {        //HTTP POSTによりアップロードされたファイルかどうかを調べる
            $this->warning = "ファイル名が不正です。";
        } else {

            $checkdata = array("GRADE_HR_CLASS_NAME"    => array(15,"クラス名"),
                               "ATTENDNO"               => array(3,"出席番号"),
                               "NAME"                   => array(120,"氏名"),
                               "YEAR"                   => array(4,"年度"),
                               "SEMESTER"               => array(1,"学期"),
                               "SCHREGNO"               => array(8,"学籍番号"),
                               "SUBCLASS"               => array(18,"科目"),
                               "SUBCLASS_NAME"          => array(90,"科目名"),
                               "VIEW"                   => array(48,"観点"),
                               "REMARK1"                => array(780,"コメント")
                               );


            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            //エラーデータの削除
            $query = $this->deleteQueryErr(PROGRAMID);
            $result = $db->query($query);

            $fp = fopen ($this->file["tmp_name"], "r");
            $i  = 0;
            $line = 1;  //行数
            $title = false;
            $header = false;
            $primaryKey1   = array();
            $duplicateCsv1 = array();
            $primaryKey1   = $this->getPrimaryKey('JVIEWSTAT_REPORTREMARK_DAT');
            while (!feof ($fp))
            {
                $buffer = fgets($fp, 4096);
                if (trim($buffer) != '')
                {

                    if (!$title){
                        $title = true;
                        $line++; //タイトル行を対象外とする
                        continue;
                    }

                    if (!$header && $this->field["HEADER"]=="on" ){
                        $header = true;
                        $line++; //ヘッダ行を対象外とする
                        continue;
                    }
                    //文字コードを判別、改行コードを削除
                    $buffer = trim(i18n_convert($buffer, i18n_http_output(), "SJIS-win"));
                    $tmp    = common::csv2array($buffer);
                    $data = array("GRADE_HR_CLASS_NAME"    => $tmp[0],
                                  "ATTENDNO"               => $tmp[1],
                                  "NAME"                   => $tmp[2],
                                  "YEAR"                   => $tmp[3],
                                  "SEMESTER"               => $tmp[4],
                                  "SCHREGNO"               => $tmp[5],
                                  "SUBCLASS"               => $tmp[6],
                                  "SUBCLASS_NAME"          => $tmp[7],
                                  "VIEW"                   => $tmp[8],
                                  "REMARK1"                => $tmp[9]
                                  );
                    if ($model->Properties["useCurriculumcd"] == '1') {
                        $subclass_array = array();
                        $subclass_array = explode("-", $tmp[6]);
                        $data["CLASSCD"]       = $subclass_array[0];
                        $data["SCHOOL_KIND"]   = $subclass_array[1];
                        $data["CURRICULUM_CD"] = $subclass_array[2];
                        $data["SUBCLASSCD"]    = $subclass_array[3];
                    } else {
                        $data["CLASSCD"]       = "";
                        $data["SCHOOL_KIND"]   = "";
                        $data["CURRICULUM_CD"] = "";
                        $data["SUBCLASSCD"]    = $model->field["SUBCLASSCD"];
                    }

                    //エラーチェック
                    if ($this->errCheck($db, $data, $checkdata, $line, $duplicateCsv1, $primaryKey1)) {
                        $temp_arr[$i] = $data;
                        $i++;
                    }
                    foreach ($primaryKey1 as $key => $val) {
                        $duplicateCsv1[$line][$val] = $data[$val];
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

    //エラーチェック
    function errCheck($db, $data, $data_each, $line, $duplicateCsv1, $primaryKey1)
    {
        $returnFlg          = true;
        $hissuErrFlg        = false;    //必須エラーフラグ
        $lengthErrFlg       = false;    //レングスエラーフラグ
        $duplicateCsvErrFlg = false;

        $hissuErrMsg        = "";       //必須エラーメッセージ
        $lengthErrMsg       = "";       //レングスエラーメッセージ
        $duplicateCsvErrMsg = "";

        $hissuSep        = "";
        $lengthSep       = "";
        $duplicateCsvSep = "";

        $dupFlg1 = $this->duplicateCsvCheck($duplicateCsv1, $primaryKey1, $data);
        if ($dupFlg1[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複1(". $dupFlg1[1] ."行目)");
        }

        foreach ($data_each as $key => $val) {

            //必須項目チェック
            if (in_array($key,array("YEAR","SEMESTER","SCHREGNO","SUBCLASS"))) {
                if (!strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                }
            }

            //文字数チェック
            if (in_array($key,array("YEAR","SEMESTER","SCHREGNO","SUBCLASS","REMARK1"))) {
                if (strlen($data[$key]) > $val[0]) {
                    $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                }
            } else if ($this->field["SHORI_MEI"] == "1") {
                if (strlen($data[$key]) > $val[0]) {
                    $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                }
            }

        }

        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$hissuErrMsg.$lengthErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knjx_d139Query::insertQueryErr($db, $line, $checkError);
        }
        return $returnFlg;
    }

    //データの整合性チェック
    function IsDataMatched(&$data_arr)
    {
        //５千件を超えたら制限エラー
        if (get_count($data_arr) > 5000) {
            $this->setWarning("MSG915","処理件数を５千件以下にして下さい 処理件数：".get_count($data_arr)."件");
            return false;
        }
        return true;

    }

    //ＣＳＶ出力
    function getDownloadModel()
    {

        $col = array();
        if ($this->field["OUTPUT"]=="3"){
            //ヘッダ(エラー出力)
            $col[]["MSGROW"]     = "行番号";
            $col[]["MSGREMARK"]  = "メッセージ";
            $col[]["VIEW"]       = "観点";
            $col[]["REMARK1"]    = "コメント";
        } else{
            //ヘッダ(データ出力)
            $col[]["GRADE_HR_CLASS_NAME"] = "クラス名";
            $col[]["ATTENDNO"]            = "出席番号";
            $col[]["NAME"]                = "氏名";
            $col[]["YEAR"]                = "年度";
            $col[]["SEMESTER"]            = "学期";
            $col[]["SCHREGNO"]            = "学籍番号";
            $col[]["SUBCLASS"]            = "科目";
            $col[]["SUBCLASS_NAME"]       = "科目名";
            $col[]["VIEW"]                = "観点";
            $col[]["REMARK1"]             = "コメント";
        }

        $semesterName  = "仮学期"; //学期
        $subClassName  = "仮教科"; //科目名
        if ($this->field["OUTPUT"]=="4"){
            $db = Query::dbCheckOut();
            $semesterName = $db->getOne(knjx_d139Query::getNameMst("NAME1","Z009",$this->field["SEMESTER"]));
            $subClassName = $db->getOne(knjx_d139Query::getSubclassName($this->field["CLASSCD"], $this->field["GRADE_HR_CLASS"], $this,$this->field["SUBCLASSCD"]));
            Query::dbCheckIn($db);
        }
        $csvTitle = CTRL_YEAR."年度　".$semesterName."　面談資料所見チェックリスト(クラス別)";

        $contents  = "";    //クリア
        if ($this->field["OUTPUT"] == "3" ) {
            $headerDuplicate = array("YEAR"          => "年度",
                                     "SEMESTER"      => "学期",
                                     "SCHREGNO"      => "学籍番号",
                                     "CLASSCD"       => "教科コード",
                                     "SCHOOL_KIND"   => "学校校種",
                                     "CURRICULUM_CD" => "教育課程コード",
                                     "SUBCLASSCD"    => "科目コード");
            $tables = array();
            $tables = array("1" => "JVIEWSTAT_REPORTREMARK_DAT");
            $dupTable = $this->getDuplicateErrorHeader(PROGRAMID, $tables);
            if (sizeof($dupTable) > 0) {
                $headerErr = $this->duplicateCsvHeader($dupTable, $headerDuplicate);
                $contents .= implode($headerErr, ",")."\n";
            }
        }
        $contents .= "　　　　".$csvTitle."\n";

        $header_data = false;   //出力フラグ
        if ($this->field["OUTPUT"]=="1") {
            //ヘッダ出力(見本)
            $header_data = true;
            $contents .= "クラス名,出席番号,氏名,年度,学期,学籍番号,科目,科目名,観点,コメント\n";
            $contents .= "1ねん1くみ,001,山田 太郎,2006,1,00000001,110001,国語,観点①,サンプルコメント\n";
        } else{
            $headerFlg = false;
            if ($this->field["OUTPUT"]=="4" && $this->field["HEADER"]!="on" ) $headerFlg = true;

            $dataCol = array();
            //ヘッダを設定
            $titleSep = "";
            foreach ($col as $renBan => $fieldVal) {
                foreach ($fieldVal as $field => $val) {
                    if (!$headerFlg) $contents .= $titleSep.$val;
                    $titleSep = ",";
                    $dataCol[$field] =  $val;
                }
            }
            if (!$headerFlg) $contents .= "\n";
        }

        //ヘッダ以外
        if ($this->field["OUTPUT"] != "1") {
            //DB接続
            $db = Query::dbCheckOut();

            if($this->field["OUTPUT"]=="3") {
                //エラー
                $query = $this->selectCsvErrQuery(PROGRAMID);
            } else if ($this->field["OUTPUT"]=="4") {
                //データ
                $query = knjx_d139Query::selectQuery($this, $subClassName);
            }

            //SQL文発行
            $result = $db->query($query);
            while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                if($this->field["OUTPUT"]=="4"){                //データ
                    $row = str_replace(","   , "."  , $row);    //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                    $row = str_replace("\r\n", "↓" , $row);
                    $row = str_replace("\r"  , "↓" , $row);
                    $row = str_replace("\n"  , "↓" , $row);
                }
                //値をセット
                $sep = "";
                foreach ($dataCol as $field => $val) {
                    if ($field == "VIEW") {
                        //観点の設定
                        $key = 1;
                        
                        $contents .= $sep;
                        
                        $viewQuery  = knjx_d139Query::selectViewcdQuery($this,$row["SCHREGNO"]);
                        $viewResult = $db->query($viewQuery);
                        while( $viewRow = $viewResult->fetchRow(DB_FETCHMODE_ASSOC)){
                            $viewRow = str_replace(","   , "."  , $viewRow);    //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                            $viewRow = str_replace("\r\n", "↓" , $viewRow);
                            $viewRow = str_replace("\r"  , "↓" , $viewRow);
                            $viewRow = str_replace("\n"  , "↓" , $viewRow);
                            if ($key == 1) {
                                $number = "①";
                            } else if ($key == 2) {
                                $number = "②";
                            } else if ($key == 3) {
                                $number = "③";
                            } else if ($key == 4) {
                                $number = "④";
                            } else if ($key == 5) {
                                $number = "⑤";
                            } else if ($key == 6) {
                                $number = "⑥";
                            }
                            $contents .= $number.$viewRow[$field];
                            $key = $key + 1;
                        }
                      $viewResult->free();
                    } else{
                        $contents .= $sep.$row[$field];
                    }
                    $sep = ",";
                }
                $contents .= "\n";
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
                $couse = $csvTitle;
            }
            $filename = sprintf("%s.csv",$couse);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }
}
?>
