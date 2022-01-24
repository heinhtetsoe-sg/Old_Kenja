<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knje061Model extends Model {
    var $field      = array();
    var $data       = array();
    var $cmd;
    var $file;
    var $annual;            //年次
    var $control;           //コントロールマスタ
    var $hr_class;
    var $coursecode;
    var $RegdTable;         //学校区分別のテーブル名
    var $schoolName;
    var $recordTableDiv;
        
    /**
     * メンバ変数の初期化
    */
    function knje061Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        if ($this->control["学校区分"] == "0") { //学年制
           $this->RegdTable = "v_regdyear_grade_dat";
        } elseif ($this->control["学校区分"] == "1") { //単位制
            $this->RegdTable = "v_regdyear_unit_dat";
        }
    }
    
    function init()
    {
        $this->cmd    = VARS::request("cmd");
        //年次
        $this->annual = VARS::post("ANNUAL");
        //組
        $this->hr_class = VARS::post("HR_CLASS");
        //コース
        $this->coursecode = VARS::post("COURSECODE");
        $this->schregno   = VARS::post("SCHREGNO");
        
        //年次が変更したら初期化する
        if ($this->cmd == "annual") {
            $this->hr_class = "";
            $this->coursecode = "";
        }
        
        if (VARS::file("FILE")){
            $this->file = VARS::file("FILE");        //ファイルのアップロード
        }
        //画面上のデータ
        $this->field = array("KIND"     => VARS::post("KIND"),         //種別
                            "RANGE"     => VARS::post("RANGE"),        //生成範囲
                            "METHOD"    => VARS::post("METHOD"),       //生成方法
                            "CREATEDIV" => VARS::post("CREATEDIV"),     //新規作成 OR 追加作成
                            "HEADERFLG" => VARS::post("HEADERFLG"));   //ヘッダ有りフラグ
        // 成績テーブルを判断するためのフラグを取得(法政・自修館・その他)
        $rtnRow = knje061Query::getNameMst();
        $this->schoolName       = $rtnRow["NAME1"];
        $this->recordTableDiv   = $rtnRow["NAMESPARE1"];
    }

    function &getMainModel()
    {
    }

    /**
     * 実行モデルを返します
    */
    function &getExecModel()
    {
        if ($this->field["KIND"] == 2 && $this->field["METHOD"] == 2){
            //ファイル取り込み
            $this->fileUpload();

        } elseif ($this->field["KIND"] == 2 && $this->field["METHOD"] == 1){ //"成績データ抽出中..."

            if ($this->field["CREATEDIV"] == 1) {
                //新規作成
                knje061Query::addRecord_Schreg_Studyrec_Dat($this);
            } elseif ($this->field["CREATEDIV"] == 2) {
                //追加作成
                knje061Query::addRecord_Schreg_Studyrec_Dat2($this);
            } else {
                //上書作成
                knje061Query::addRecord_Schreg_Studyrec_Dat3($this);
            }
            // 広島国際の場合のみ「総合学習(900100)とＬＨＲ(940000)」
            if ($this->schoolName == "hirogaku") {
                knje061Query::hirogakuStudyRec($this);
            }
            $this->message = "学籍学習記録データの生成が完了しました。";
        
        } elseif ($this->field["KIND"] == 1){ //"勤怠データ抽出＆集計中..."
            //出欠学期別累積データより集計
            knje061Query::addRecord_Schreg_Attendrec_Dat($this);
            $this->message = "学籍出欠記録データの生成が完了しました。";
        }
    }

    function fileUpload()
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        if (is_uploaded_file ($this->file["tmp_name"])){
            $fp = fopen ($this->file["tmp_name"], "r");
           //学籍学習記録データのサイズ
            $size =   array(1,0,0,0,8,0,0,0,4,2,0,0,0,0,2,30,15,40,30,6,60,9,40,20,2,2,2,2,60);

            $i = $j = 0;
            $err = array();
            $header = false;
            while (!feof ($fp)) {
                $buffer = fgets($fp, 4096);

                if (trim($buffer) != ''){
                    //ヘッダ有りの場合は1行目を無視
                    if ($this->field["HEADERFLG"] == "1" && !$header) {
                        $header = true;
                        continue;
                    }

                    $buffer = trim(i18n_convert($buffer, i18n_http_output(), "SJIS-win"));
                    $tmp = common::csv2array($buffer);
                    $validate = true;

                    //必須項目チェック
                    if (!strlen($tmp[0]) | !strlen($tmp[4]) | !strlen($tmp[8]) | !strlen($tmp[9]) | !strlen($tmp[14]) | !strlen($tmp[19])) {
                        $validate = false;
                    }

                    foreach($tmp as $key => $val) {

                        if (in_array($key, array(1,2,3,5,6,7,10,11,12,13,29))){ //無視するフィールド
                        //属性がVARCHAR
                        }else{
                           if (strlen($val) > $size[$key]){
                               $validate = false;
                               break;
                           }
                        }

                    }
                    if ($validate){
                        $data = array("SCHOOLCD"        => $tmp[0],
                                      "YEAR"            => $tmp[8],
                                      "SCHREGNO"        => $tmp[4],
                                      "ANNUAL"          => $tmp[9],
                                      "CLASSCD"         => $tmp[14],
                                      "SUBCLASSCD"      => $tmp[19],
                                      "CLASSNAME"       => $tmp[15],
                                      "CLASSABBV"       => $tmp[16],
                                      "CLASSNAME_ENG"   => $tmp[17],
                                      "CLASSABBV_ENG"   => $tmp[18],
                                      "SUBCLASSNAME"    => $tmp[20],
                                      "SUBCLASSABBV"    => $tmp[21],
                                      "SUBCLASSNAME_ENG"=> $tmp[22],
                                      "SUBCLASSABBV_ENG"=> $tmp[23],
                                      "VALUATION"       => $tmp[24],
                                      "GET_CREDIT"      => $tmp[25],
                                      "ADD_CREDIT"      => $tmp[26],
                                      "COMP_CREDIT"     => $tmp[27],
                                      "REMARK"          => $tmp[28]);

                        $data = str_replace("'", "''", $data);

                        $db->expectError();
                        //学籍学習記録データの削除
                        $result = $db->query(knje061Query::deleteQuerySchreg_StudyRec_Dat($data));
                        if (PEAR::isError($result))  continue;

                        //CLASS_MSTより科目数を取得
                        $subclasses = $db->getOne(knje061Query::getSubclasses($data));
                        $data["SUBCLASSES"] = $subclasses;

                        //学籍学習記録データの作成（ＣＳＶデータより読込)
                        $result = $db->query(knje061Query::addQuerySchreg_StudyRec_Dat($data));
                        if (PEAR::isError($result)) continue;
                        $result = $db->query(knje061Query::deleteStudyRecRemark($data));
                        if (PEAR::isError($result)) continue;
                        $result = $db->query(knje061Query::insertStudyRecRemark($data));
                        if (PEAR::isError($result)) continue;
                        $db->popExpect();
                        $i++;
                    }else{
                        $err[] = $j+1;
                    }
                    $j++;
                }
            }
            $db->commit();
            Query::dbCheckIn($db);
            fclose($fp);

            if ($i > 0){
                $this->message = $i."行 CSV登録に成功しました。\\n";
                if (get_count($err) > 0){
                    $this->message .= "失敗した行：" .implode($err, ",");
                }
            } else {
                $this->message = "CSV登録に失敗しました。";
            }
        } else {
            $this->warning = "ファイル名が不正です。";
        }
    }

    //テンプレートの書き出し
    function OutputTmpFile() {
        $contents  ="※本校区分,学年,組,出席番号,※学籍番号,生徒氏名,性別区分,性別,";
        $contents .="※年度,※履修学年,課程コード,学科コード,コースコード,コースコード名,";
        $contents .="※教科コード,教科名,教科略称,教科名英字,教科略称英字,";
        $contents .="※科目コード,科目名,科目略称,科目名英字,科目略称英字,";
        $contents .="評価,標準修得単位,増加修得単位,履修単位,履修科目備考,DUMMY\n";
        $contents .="0,01,001,001,20051003,今井花子,2,女性,";
        $contents .="2005,01,1,001,0001,文Ⅰ,";
        $contents .="13,公民,公民,Civics,Civ,";
        $contents .="130001,現代社会,現代社,Civics,Civ,";
        $contents .="2,2,2,2,備考です,DUMMY";

        $filename = sprintf("%s.csv","学籍学習記録データ");
        common::downloadContents($contents, $filename);
        return true;
    }
}
?>
