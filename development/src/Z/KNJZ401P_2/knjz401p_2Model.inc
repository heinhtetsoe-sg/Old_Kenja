<?php

require_once('for_php7.php');
//定数定義

class knjz401p_2Model extends Model
{
    public $field  = array();
    public $cmd;
    public $grade  = "";        //primary
    public $subclasscd = "";    //primary
    public $viewcd = "";        //primary
    public $year_code = "";
    //教育課程対応
    public $classcd = "";
    public $school_kind = "";
    public $curriculum_cd = "";
    public $file;
    public $lastColumn;//CSVの最後の列の値

    //メンバ変数の初期化
    public function knjz401p_2Model()
    {
    }

    public function init()
    {
        $viewcd = $classcd = "" ;
        //観点コードセット用教科コード
        if (VARS::post("CLASSCD") != "") {
            $classcd = substr(VARS::post("CLASSCD"), 0, 2);
        }
        //コードのゼロ埋め
        if (VARS::post("VIEWCD") != "") {
            $viewcd = sprintf("%02d", VARS::post("VIEWCD"));
        }

        $this->field = array("CLASSCD"          =>  VARS::post("CLASSCD"),          //教科
                             //教育課程用
                             "SCHOOL_KIND"      =>  VARS::post("SCHOOL_KIND"),      //学校種別
                             "CURRICULUM_CD"    =>  VARS::post("CURRICULUM_CD"),    //教育課程コード
        
                             "SUBCLASSCD"       =>  VARS::post("SUBCLASSCD"),       //科目
                             "VIEWCD"           =>  $classcd.$viewcd,               //観点コード
                             
                             "RECORD_DIV"        =>  VARS::post("RECORD_DIV"),      //クラス形態
                             //DETAILのFIELD(学期で持つ)
                             "VIEWNAME1"         =>  VARS::post("VIEWNAME1"),       //観点名
                             "VIEWNAME2"         =>  VARS::post("VIEWNAME2"),       //観点名
                             "VIEWNAME3"         =>  VARS::post("VIEWNAME3"),       //観点名
                             "VIEWABBV1"         =>  VARS::post("VIEWABBV1"),       //観点略称
                             "VIEWABBV2"         =>  VARS::post("VIEWABBV2"),       //観点略称
                             "VIEWABBV3"         =>  VARS::post("VIEWABBV3"),       //観点略称
                             "SHOWORDER1"        =>  VARS::post("SHOWORDER1"),        //表示順
                             "SHOWORDER2"        =>  VARS::post("SHOWORDER2"),        //表示順
                             "SHOWORDER3"        =>  VARS::post("SHOWORDER3"),        //表示順
                             "STUDYREC_SUBCLASSCD1"  =>  VARS::post("STUDYREC_SUBCLASSCD1"),  //指導要録用科目コード
                             "STUDYREC_SUBCLASSCD2"  =>  VARS::post("STUDYREC_SUBCLASSCD2"),  //指導要録用科目コード
                             "STUDYREC_SUBCLASSCD3"  =>  VARS::post("STUDYREC_SUBCLASSCD3"),  //指導要録用科目コード
                             "STUDYREC_VIEWCD1"  =>  VARS::post("STUDYREC_VIEWCD1"),  //指導要録用観点コード
                             "STUDYREC_VIEWCD2"  =>  VARS::post("STUDYREC_VIEWCD2"),  //指導要録用観点コード
                             "STUDYREC_VIEWCD3"  =>  VARS::post("STUDYREC_VIEWCD3"),  //指導要録用観点コード
                             "GRADE"            =>  VARS::post("GRADE2"),           //学年
                             "UPDATED"          =>  VARS::post("UPDATED"),          //更新日付
                             "R_GRADE"          =>  VARS::post("R_GRADE"),          //参照学年
                             "year_code"        =>  VARS::post("year_code"),        //前画面の年度情報保持
                             "HEADER"           =>  VARS::post("HEADER"),
                             "OUTPUT"           =>  VARS::post("OUTPUT")
                            );

        $this->cmd = VARS::request("cmd");

        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->file = VARS::file("FILE");

        if (VARS::get("year_code")) {
            unset($this->grade);
            unset($this->subclasscd);
            unset($this->viewcd);
        }
        if (VARS::get("mode") == "1") {
            unset($this->grade);
            unset($this->subclasscd);
            unset($this->viewcd);
        }
        if (VARS::get("SUBCLASSCD") != "") {
            $this->subclasscd = VARS::get("SUBCLASSCD");
            //教育課程対応
            if ($this->Properties["useCurriculumcd"] == '1') {
                $this->classcd = VARS::get("CLASSCD");
                $this->school_kind = VARS::get("SCHOOL_KIND");
                $this->curriculum_cd = VARS::get("CURRICULUM_CD");
            }
        }
        if (VARS::get("VIEWCD") != "") {
            $this->viewcd = VARS::get("VIEWCD");
        }
        if (VARS::post("GRADE") != "") {
            $this->grade = VARS::post("GRADE");
        }
        
        //教育課程対応 VIEWCD加工用 ※もともとpostするときにclasscd+viewcdにしているため、教育課程の時おかしくなる
        if ($this->Properties["useCurriculumcd"] == '1') {
            if (strlen($this->viewcd) > 4) {
                $this->viewcd = substr($this->classcd, 0, 2).substr($this->viewcd, 4, 2);
            }
            if (strlen($this->field["VIEWCD"]) > 4) {
                $this->field["VIEWCD"] = substr($this->field["CLASSCD"], 0, 2).substr($this->field["VIEWCD"], 4, 2);
            }
        }
        
        $db = Query::dbCheckOut();
        $grade = $db->getOne(knjz401p_2Query::getGrade());
        $this->grade = ($this->grade) ? $this->grade : $grade;
        //$this->school_kind = $db->getOne(knjz401p_2Query::getSchoolKind($this->grade));
        Query::dbCheckIn($db);

        $this->lastColumn = 'DUMMY';
        if ($this->Properties["csv_LastColumn"] != '') {
            $this->lastColumn = $this->Properties["csv_LastColumn"];
        }
    }

    //入力項目チェック
    public function validateInput($kind)
    {
        $db = Query::dbCheckOut();
        //１レコード取得
        //教育課程対応
        if ($this->Properties["useCurriculumcd"] == '1') {
            $row = $db->getRow(knjz401p_2Query::getRow2($this->grade, $this->field["SUBCLASSCD"], $this->field["VIEWCD"], $this), DB_FETCHMODE_ASSOC);
        } else {
            $row = $db->getRow(knjz401p_2Query::getRow($this->grade, $this->field["SUBCLASSCD"], $this->field["VIEWCD"], $this), DB_FETCHMODE_ASSOC);
        }
        Query::dbCheckIn($db);

        if ($kind=="update" || $kind=="delete") {       //リストより選択していない
            if ($this->viewcd == "" || $this->grade == "") {
                $this->setWarning("MSG308", "キー値は変更できません");
                return false;
            }
            //キー値が変更されていますかをチェック
            if (($this->field["VIEWCD"] != $this->viewcd) || ($this->field["GRADE"] != $this->grade)) {
                $this->setWarning("MSG308", "キー値は変更できません");
                return false;
            }
            //更新日付チェック1
            if ($this->cmd == "update" && $row["UPDATED"] == "") {
                $this->setWarning("MSG303");
                return false;
            }
            //更新日付チェック2
            if ($this->cmd == "update" && $row["UPDATED"] != $this->field["UPDATED"]) {
                $this->setWarning("MSG307");
                return false;
            }
        }
        if ($kind=="update" || $kind=="insert") {
            if (!$this->grade) {
                $this->setWarning("MSG301", "(対象学年)");
                return false;
            }
            if (trim($this->field["VIEWCD"]) < 100) {
                $this->setWarning("MSG301", "(観点コード)");
                return false;
            }
            if (substr($this->field["VIEWCD"], 2) == 99) {
                $this->setWarning("MSG901", "(観点コード：99は使用不可です。99以外を入力して下さい。)");
                return false;
            }
            //重複チェック
            if ($this->cmd == "add" && is_array($row)) {
                $this->setWarning("MSG918", "(観点コード)");
                return false;
            }
            if (strlen($this->field["VIEWNAME1"]) > 210 || strlen($this->field["VIEWNAME2"]) > 210) {
                $this->setWarning("MSG915", "(観点名は210バイトまでです)");
                return false;
            } elseif (strlen($this->field["VIEWNAME1"]) == 0 && strlen($this->field["VIEWNAME2"]) == 0) {
                if ($this->getSemesterCount > 2) {
                    if (strlen($this->field["VIEWNAME1"]) == 0) {
                        $this->setWarning("MSG301", "(観点名称)");
                        return false;
                    }
                } else {
                    $this->setWarning("MSG301", "(観点名称)");
                    return false;
                }
            } elseif (strlen($this->field["VIEWABBV1"]) > 48 || strlen($this->field["VIEWABBV2"]) > 48) {
                $this->setWarning("MSG915", "(観点略称は48バイトまでです)");
                return false;
            } elseif (strlen($this->field["SHOWORDER1"]) > 2 || strlen($this->field["SHOWORDER2"]) > 2) {
                $this->setWarning("MSG915", "(表示順)");
                return false;
            }
            if ($this->getSemesterCount > 2) {
                if (strlen($this->field["VIEWNAME3"]) > 210) {
                    $this->setWarning("MSG915", "(観点名は210バイトまでです)");
                    return false;
                }
                if (strlen($this->field["VIEWABBV3"]) > 48) {
                    $this->setWarning("MSG915", "(観点略称は48バイトまでです)");
                    return false;
                }
                if (strlen($this->field["SHOWORDER3"]) > 2) {
                    $this->setWarning("MSG915", "(表示順)");
                    return false;
                }
            }
            
            //教育課程対応
            /*if ($this->Properties["useCurriculumcd"] == '1') {
                if (substr($this->field["SUBCLASSCD"], 3, 1) != substr($this->field["STUDYREC_SUBCLASSCD"], 3, 1)) {
                    $this->setWarning("MSG901","学校種別が一致していません。");
                    return false;
                }
            }*/
        }
        return true;
    }

    //追加用のモデルを返します
    public function &getInsertModel()
    {
        //入力チェック
        if ($this->validateInput("insert")) {
            $this->insert();
            $this->subclasscd = $this->field["SUBCLASSCD"];
            $this->viewcd = $this->field["VIEWCD"];
        }
        return;
    }

    //追加
    public function &insert()
    {
        $result = knjz401p_2Query::getInsertQuery($this->grade, $this->field, $this);
        if (!isset($this->warning)) {
            $this->setMessage("MSG201");
        }
        return $result;
    }

    //更新用のモデルを返します
    public function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput("update")) {
            $this->update();
        }
    }

    //更新
    public function &update()
    {
        $result = knjz401p_2Query::getUpdateQuery($this->grade, $this->field, $this);
        if (!isset($this->warning)) {
            $this->setMessage("MSG201");
        }
        return $result;
    }

    //削除用のモデルを返します
    public function &getDeleteModel()
    {
        //入力チェック
        if ($this->validateInput("delete")) {
            $this->delete();
        }
        return;
    }

    //削除
    public function &delete()
    {
        if (!isset($this->viewcd)) {
            $this->setWarning("MSG303");
            return;
        }
        $result = knjz401p_2Query::getDeleteQuery($this->grade, $this->field, $this);
        if ($result==true) {
            if (!isset($this->warning)) {
                $this->setMessage("MSG201");
            }
            $this->viewcd = "";
        } else {
            $this->setWarning("MSG919", "年度データに登録されています。");
        }
        return $result;
    }

    //コピー
    public function &getCopyModel()
    {
        $db = Query::dbCheckOut();
        $data  = $db->getOne(knjz401p_2Query::checkJViewMst($this->grade));
        $ydata = $db->getOne(knjz401p_2Query::checkJViewYdata($this, "", ""));
        $moto_data  = $db->getOne(knjz401p_2Query::checkJViewMst($this->field["R_GRADE"]));
        Query::dbCheckIn($db);

        //入力チェック
        if (!$this->grade || !$this->field["R_GRADE"]) {
            $this->setWarning("MSG304", "（学年）");
        } elseif ($data > 0) {
            $this->setWarning("対象学年にデータが存在しています。");
        } elseif ($ydata != 0) {
            $this->setWarning("対象学年に年度データが存在しています。");
        } elseif ($moto_data == 0) {
            $this->setWarning("参照学年にデータがありません。");
        } else {
            knjz401p_2Query::getCopyQuery($this);
            $this->setMessage("MSG201");
        }
        return;
    }

    //変数をクリアします
    public function clean()
    {
    }


    /***********/
    /* CSV出力 */
    /***********/
    public function getDownloadCsvModel()
    {
        $db = Query::dbCheckOut();
        $data = false;

        if ($this->field["OUTPUT"] == "3") {        //エラー出力
            $duplHeader = array("GRADE"         => "学年",
                                "CLASSCD"       => "教科コード",
                                "SCHOOL_KIND"   => "校種コード",
                                "CURRICULUM_CD" => "教育課程コード",
                                "SUBCLASSCD"    => "科目コード",
                                "VIEWCD"        => "※観点コード1",
                                "VIEW_SEQ"      => "SEQ"
                            );
            //ヘッダ（エラー出力）
            $header = array("MSGROW"    => "行番号",
                            "MSGREMARK" => "メッセージ");
            $tables = array();
            $tables = array("1" => "JVIEWNAME_GRADE_MST", "2" => "JVIEWNAME_GRADE_DETAIL_MST");
            $dupTable = $this->getDuplicateErrorHeader(PROGRAMID, $tables);
            if (sizeof($dupTable) > 0) {
                $headerErr = $this->duplicateCsvHeader($dupTable, $duplHeader);
                $contents .= implode($headerErr, ",")."\n";
            }
            //ヘッダをセット
            $contents .= implode(",", $header) . "\r\n";

            //エラー出力データ
            $query = $this->selectCsvErrQuery(PROGRAMID);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                //ヘッダ項目のみデータをセット
                foreach ($header as $key => $val) {
                    $contents .= $row[$key].",";
                }
                $contents .= "\r\n";
                $data = true;
            }
            $result->free();
        } elseif ($this->field["OUTPUT"] == "4") {     //ヘッダ出力（見本）
            if ($this->Properties["useCurriculumcd"] == '1') {
                //学期チェック
                if ($this->getSemesterCount > 2) {
                    //ヘッダ項目
                    $header = array("GRADE"             => "※学年",
                                    "CLASSCD"           => "※教科コード",
                                    "SCHOOL_KIND"       => "※校種コード",
                                    "CURRICULUM_CD"     => "※教育課程コード",
                                    "SUBCLASSCD"        => "※科目コード",
                                    "CLASSNAME"         => "教科名",
                                    "SUBCLASSNAME"      => "科目名",
                                    "VIEWCD"            => "※観点コード1",
                                    "RECORD_DIV"        => "※クラス形態",
                                    "VIEWNAME1"         => "観点名称1",
                                    "VIEWNAME2"         => "観点名称2",
                                    "VIEWNAME3"         => "観点名称3",
                                    $this->lastColumn   => $this->lastColumn
                                    );
                    //ヘッダ例
                    $header_rei = array("GRADE"             => "01",
                                        "CLASSCD"           => "12",
                                        "SCHOOL_KIND"       => "J",
                                        "CURRICULUM_CD"     => "1",
                                        "SUBCLASSCD"        => "123456",
                                        "CLASSNAME"         => "国語",
                                        "SUBCLASSNAME"      => "1年国語",
                                        "VIEWCD"            => "12",
                                        "RECORD_DIV"        => "1",
                                        "VIEWNAME1"         => "国語への関心・意欲・態度",
                                        "VIEWNAME2"         => "話す･聞く能力",
                                        "VIEWNAME3"         => "書く能力",
                                        $this->lastColumn   => $this->lastColumn
                                        );
                } else {
                    //ヘッダ項目
                    $header = array("GRADE"             => "※学年",
                                    "CLASSCD"           => "※教科コード",
                                    "SCHOOL_KIND"       => "※校種コード",
                                    "CURRICULUM_CD"     => "※教育課程コード",
                                    "SUBCLASSCD"        => "※科目コード",
                                    "CLASSNAME"         => "教科名",
                                    "SUBCLASSNAME"      => "科目名",
                                    "VIEWCD"            => "※観点コード1",
                                    "RECORD_DIV"        => "※クラス形態",
                                    "VIEWNAME1"         => "観点名称1",
                                    "VIEWNAME2"         => "観点名称2",
                                    $this->lastColumn   => $this->lastColumn
                                    );
                    //ヘッダ例
                    $header_rei = array("GRADE"             => "01",
                                        "CLASSCD"           => "12",
                                        "SCHOOL_KIND"       => "J",
                                        "CURRICULUM_CD"     => "1",
                                        "SUBCLASSCD"        => "123456",
                                        "CLASSNAME"         => "国語",
                                        "SUBCLASSNAME"      => "1年国語",
                                        "VIEWCD"            => "12",
                                        "RECORD_DIV"        => "1",
                                        "VIEWNAME1"         => "国語への関心・意欲・態度",
                                        "VIEWNAME2"         => "話す･聞く能力",
                                        $this->lastColumn   => $this->lastColumn
                                        );
                }
            } else {
                //学期チェック
                if ($this->getSemesterCount > 2) {
                    //ヘッダ項目
                    $header = array("GRADE"             => "※学年",
                                    "CLASSCD"           => "※教科コード",
                                    "SUBCLASSCD"        => "※科目コード",
                                    "CLASSNAME"         => "教科名",
                                    "SUBCLASSNAME"      => "科目名",
                                    "VIEWCD"            => "※観点コード1",
                                    "RECORD_DIV"        => "※クラス形態",
                                    "VIEWNAME"          => "観点名称1",
                                    "VIEWNAME2"         => "観点名称2",
                                    "VIEWNAME2"         => "観点名称3",
                                    $this->lastColumn   => $this->lastColumn
                                    );
                    //ヘッダ例
                    $header_rei = array("GRADE"             => "01",
                                        "CLASSCD"           => "12",
                                        "SUBCLASSCD"        => "123456",
                                        "CLASSNAME"         => "国語",
                                        "SUBCLASSNAME"      => "1年国語",
                                        "VIEWCD"            => "12",
                                        "RECORD_DIV"        => "1",
                                        "VIEWNAME1"         => "国語への関心・意欲・態度",
                                        "VIEWNAME2"         => "話す･聞く能力",
                                        "VIEWNAME3"         => "書く能力",
                                        $this->lastColumn   => $this->lastColumn
                                        );
                } else {
                    //ヘッダ項目
                    $header = array("GRADE"             => "※学年",
                                    "CLASSCD"           => "※教科コード",
                                    "SUBCLASSCD"        => "※科目コード",
                                    "CLASSNAME"         => "教科名",
                                    "SUBCLASSNAME"      => "科目名",
                                    "VIEWCD"            => "※観点コード1",
                                    "RECORD_DIV"        => "※クラス形態",
                                    "VIEWNAME"          => "観点名称1",
                                    "VIEWNAME2"         => "観点名称2",
                                    $this->lastColumn   => $this->lastColumn
                                    );
                    //ヘッダ例
                    $header_rei = array("GRADE"             => "01",
                                        "CLASSCD"           => "12",
                                        "SUBCLASSCD"        => "123456",
                                        "CLASSNAME"         => "国語",
                                        "SUBCLASSNAME"      => "1年国語",
                                        "VIEWCD"            => "12",
                                        "RECORD_DIV"        => "1",
                                        "VIEWNAME1"         => "国語への関心・意欲・態度",
                                        "VIEWNAME2"         => "話す･聞く能力",
                                        $this->lastColumn   => $this->lastColumn
                                        );
                }
            }

            //ヘッダ有チェックボックス
            if ($this->field["HEADER"]) {
                //ヘッダをセット
                $contents = implode(",", $header) . "\r\n";
            } else {
                $contents = "";
            }

            //ヘッダ例をセット
            $contents .= implode(",", $header_rei) . "\r\n";
            $data = true;
        } elseif ($this->field["OUTPUT"] == "2") {     //書出（CSVデータ出力）
            $dataTemp = array();
            $tempCnt = array();

            //CSV出力データ取得
            $query = knjz401p_2Query::getCsvData($this);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $row = str_replace(",", ".", $row);   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。

                if ($this->Properties["useCurriculumcd"] == '1') {
                    $key = $row["GRADE"].$row["CLASSCD"].$row["SCHOOL_KIND"].$row["CURRICULUM_CD"].$row["SUBCLASSCD"];

                    if ($dataTemp[$key] == "") {
                        $dataTemp[$key][] = $row["GRADE"];
                        $dataTemp[$key][] = $row["CLASSCD"];
                        $dataTemp[$key][] = $row["SCHOOL_KIND"];
                        $dataTemp[$key][] = $row["CURRICULUM_CD"];
                        $dataTemp[$key][] = $row["SUBCLASSCD"];
                        $dataTemp[$key][] = $row["CLASSNAME"];
                        $dataTemp[$key][] = $row["SUBCLASSNAME"];
                        $dataTemp[$key][] = $row["VIEWCD"];
                        $dataTemp[$key][] = $row["RECORD_DIV"];
                        $dataTemp[$key][] = $row["VIEWNAME1"];
                        $dataTemp[$key][] = $row["VIEWNAME2"];
                        //学期チェック
                        if ($this->getSemesterCount > 2) {
                            $dataTemp[$key][] = $row["VIEWNAME3"];
                        }
                    } else {
                        $dataTemp[$key][] = $row["VIEWCD"];
                        $dataTemp[$key][] = $row["RECORD_DIV"];
                        $dataTemp[$key][] = $row["VIEWNAME1"];
                        $dataTemp[$key][] = $row["VIEWNAME2"];
                        //学期チェック
                        if ($this->getSemesterCount > 2) {
                            $dataTemp[$key][] = $row["VIEWNAME3"];
                        }
                    }
                } else {
                    $key = $row["GRADE"].$row["SUBCLASSCD"];

                    if ($dataTemp[$key] == "") {
                        $dataTemp[$key][] = $row["GRADE"];
                        $dataTemp[$key][] = $row["CLASSCD"];
                        $dataTemp[$key][] = $row["SUBCLASSCD"];
                        $dataTemp[$key][] = $row["CLASSNAME"];
                        $dataTemp[$key][] = $row["SUBCLASSNAME"];
                        $dataTemp[$key][] = $row["VIEWCD"];
                        $dataTemp[$key][] = $row["RECORD_DIV"];
                        $dataTemp[$key][] = $row["VIEWNAME1"];
                        $dataTemp[$key][] = $row["VIEWNAME2"];
                        //学期チェック
                        if ($this->getSemesterCount > 2) {
                            $dataTemp[$key][] = $row["VIEWNAME3"];
                        }
                    } else {
                        $dataTemp[$key][] = $row["VIEWCD"];
                        $dataTemp[$key][] = $row["RECORD_DIV"];
                        $dataTemp[$key][] = $row["VIEWNAME1"];
                        $dataTemp[$key][] = $row["VIEWNAME2"];
                        //学期チェック
                        if ($this->getSemesterCount > 2) {
                            $dataTemp[$key][] = $row["VIEWNAME3"];
                        }
                    }
                }
                $tempCnt[$key]++;
            }
            $result->free();

            if ($this->Properties["useCurriculumcd"] == '1') {
                //ヘッダ項目
                $header = array("GRADE"             => "※学年",
                                "CLASSCD"           => "※教科コード",
                                "SCHOOL_KIND"       => "※校種コード",
                                "CURRICULUM_CD"     => "※教育課程コード",
                                "SUBCLASSCD"        => "※科目コード",
                                "CLASSNAME"         => "教科名",
                                "SUBCLASSNAME"      => "科目名"
                                );
            } else {
                //ヘッダ項目
                $header = array("GRADE"             => "※学年",
                                "CLASSCD"           => "※教科コード",
                                "SUBCLASSCD"        => "※科目コード",
                                "CLASSNAME"         => "教科名",
                                "SUBCLASSNAME"      => "科目名"
                                );
            }

            //データが出力されるMAX列数を取得
            $headCnt = 0;
            foreach ($tempCnt as $key => $val) {
                $headCnt = ($headCnt < $val) ? $val : $headCnt;
            }
            //MAX列数分のヘッダを取得
            for ($i = 0; $i < $headCnt; $i++) {
                $header["VIEWCD".$i]        = "※観点コード".($i+1);
                $header["RECORD_DIV".$i]    = "※クラス形態".($i+1);
                $header["VIEWNAME1".$i]      = "観点名称1".($i+1);
                $header["VIEWNAME2".$i]      = "観点名称2".($i+1);
                //学期チェック
                if ($this->getSemesterCount > 2) {
                    $header["VIEWNAME3".$i]      = "観点名称3".($i+1);
                }
            }

            $header[$this->lastColumn] = $this->lastColumn;

            //ヘッダ有チェックボックス
            if ($this->field["HEADER"]) {
                //ヘッダをセット
                $contents = implode(",", $header) . "\r\n";
            } else {
                $contents = "";
            }

            //ＣＳＶデータをセット
            foreach ($dataTemp as $key => $val) {
                $contents .= implode($val, ",").",".$this->lastColumn."\r\n";
                $data = true;
            }
        }

        Query::dbCheckIn($db);

        if ($data) {
            if ($this->field["OUTPUT"] == "3") {
                $couse="エラー出力";
            } else {
                $couse="科目別観点マスタ（通知票用）";
            }
            $filename = sprintf("%s.csv", $couse);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }


    /***********/
    /* CSV取込 */
    /***********/
    public function getUploadCsvModel()
    {

        //ファイルの文字列の取得
        $this->data_arr = array();
        $this->data_arr = $this->getFileString();
        //データの整合性チェック
        if ($this->isDataMatched($this->data_arr)) {
            $errFlg = $this->checkCsvErrQuery(PROGRAMID);
            if ($errFlg) {
                $db = Query::dbCheckOut();
                $db->autoCommit(false);
                //更新（処理件数取得）
                knjz401p_2Query::insertQueryCsv($db, $this, $this->data_arr);
                $db->commit();
                Query::dbCheckIn($db);
                $this->setMessage("MSG202", "更新件数".get_count($this->data_arr)."件");
            }
        }
    }

    //送られたファイルの文字列を取得
    public function getFileString()
    {
        $temp_arr = array();
        if (!is_uploaded_file($this->file["tmp_name"])) {   //HTTP POSTによりアップロードされたファイルかどうかを調べる
            $this->warning = "ファイル名が不正です。";
        } else {
            //チェック対象項目（フィールド名 => サイズ, 項目名（メッセージ用））
            if ($this->Properties["useCurriculumcd"] == '1') {
                $checkdata = array("GRADE"              => array(2, "学年"),
                                   "CLASSCD"            => array(2, "教科コード"),
                                   "SCHOOL_KIND"        => array(1, "校種コード"),
                                   "CURRICULUM_CD"      => array(1, "教育課程コード"),
                                   "SUBCLASSCD"         => array(6, "科目コード")
                                   );
            } else {
                $checkdata = array("GRADE"              => array(2, "学年"),
                                   "CLASSCD"            => array(2, "教科コード"),
                                   "SUBCLASSCD"         => array(6, "科目コード")
                                   );
            }

            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            //エラーデータの削除
            $query = $this->deleteQueryErr(PROGRAMID);
            $db->query($query);

            $fp = fopen($this->file["tmp_name"], "r");
            $i  = 0;
            $line = 1;  //行数
            $header = false;
            $primaryKey1 = array();
            $primaryKey2 = array();
            $duplicateCsv1 = array();
            $duplicateCsv2 = array();
            $primaryKey1 = $this->getPrimaryKey('JVIEWNAME_GRADE_MST');
            $primaryKey2 = $this->getPrimaryKey('JVIEWNAME_GRADE_DETAIL_MST');
            while (!feof($fp)) {
                $buffer = fgets($fp, 4096);
                if (trim($buffer) != '') {
                    //ヘッダ有の場合、1行目をスキップ
                    if (!$header && $this->field["HEADER"] == "on") {
                        $header = true;
                        $line++;
                        continue;
                    }
                    //文字コードを判別、改行コードを削除
                    $buffer = trim(i18n_convert($buffer, i18n_http_output(), "SJIS-win"));
                    $tmp    = common::csv2array($buffer);

                    //データ項目＆値セット
                    if ($this->Properties["useCurriculumcd"] == '1') {
                        $data = array("GRADE"           => (strlen(trim($tmp[0]))) ? sprintf("%02d", $tmp[0]) : "",
                                      "CLASSCD"         => (strlen(trim($tmp[1]))) ? sprintf("%02d", $tmp[1]) : "",
                                      "SCHOOL_KIND"     => $tmp[2],
                                      "CURRICULUM_CD"   => $tmp[3],
                                      "SUBCLASSCD"      => (strlen(trim($tmp[4]))) ? sprintf("%06d", $tmp[4]) : ""
                                     );
                        $s_no = 7;
                    } else {
                        $data = array("GRADE"           => (strlen(trim($tmp[0]))) ? sprintf("%02d", $tmp[0]) : "",
                                      "CLASSCD"         => (strlen(trim($tmp[1]))) ? sprintf("%02d", $tmp[1]) : "",
                                      "SUBCLASSCD"      => (strlen(trim($tmp[2]))) ? sprintf("%06d", $tmp[2]) : ""
                                     );
                        $s_no = 5;
                    }
                    $col_no = 0;
                    $seki = 1;
                    for ($j = $s_no; $j <=get_count($tmp); $j++) {
                        if ($tmp[$j] == $this->lastColumn) {
                            break;
                        }
                        //学期チェック
                        if ($this->getSemesterCount > 2) {
                            if (5 * $seki + 2 == $j) {
                                $col_no++;
                                $seki++;
                                $data["VIEWCD".$col_no]     = (strlen(trim($tmp[$j]))) ? sprintf("%04d", $tmp[$j]) : "";
                                $data["RECORD_DIV".$col_no] = (strlen(trim($tmp[$j+1]))) ? $tmp[$j+1] : "";
                                $data["VIEWNAME1".$col_no]  = (strlen(trim($tmp[$j+2]))) ? $tmp[$j+2] : "";
                                $data["VIEWNAME2".$col_no]  = (strlen(trim($tmp[$j+3]))) ? $tmp[$j+3] : "";
                                $data["VIEWNAME3".$col_no]  = ($tmp[$j+4] == $this->lastColumn) ? "" : $tmp[$j+4];
                            }
                        } else {
                            if (4 * $seki + 3 == $j) {
                                $col_no++;
                                $seki++;
                                $data["VIEWCD".$col_no]     = (strlen(trim($tmp[$j]))) ? sprintf("%04d", $tmp[$j]) : "";
                                $data["RECORD_DIV".$col_no] = (strlen(trim($tmp[$j+1]))) ? $tmp[$j+1] : "";
                                $data["VIEWNAME1".$col_no]  = (strlen(trim($tmp[$j+2]))) ? $tmp[$j+2] : "";
                                $data["VIEWNAME2".$col_no]  = ($tmp[$j+3] == $this->lastColumn) ? "" : $tmp[$j+3];
                            }
                        }
                    }

                    //チェック対象項目（フィールド名 = サイズ, 項目名（メッセージ用））
                    for ($j = 1; $j <= $col_no; $j++) {
                        $checkdata["VIEWCD".$j]     = array(4, "観点コード".$j);
                        $checkdata["RECORD_DIV".$j]  = array(1, "クラス形態".$j);
                        $checkdata["VIEWNAME1".$j]   = array(210, "観点名称1".$j);
                        $checkdata["VIEWNAME2".$j]   = array(210, "観点名称2".$j);
                        //学期チェック
                        if ($this->getSemesterCount > 2) {
                            $checkdata["VIEWNAME2".$j]   = array(210, "観点名称3".$j);
                        }
                    }

                    //エラーチェック
                    if ($this->errCheck($db, $data, $checkdata, $col_no, $line, $duplicateCsv1, $duplicateCsv2, $primaryKey1, $primaryKey2)) {
                        $temp_arr[$i] = $data;
                        $temp_arr[$i]["COL_NO"] = $col_no;
                        $i++;
                    }
                    foreach ($primaryKey1 as $key => $val) {
                        $duplicateCsv1[$line][$val] = $data[$val];
                    }
                    foreach ($primaryKey2 as $key => $val) {
                        $duplicateCsv2[$line][$val] = $data[$val];
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
    public function errCheck($db, $data, $checkdata, $col_no, $line, $duplicateCsv1, $duplicateCsv2, $primaryKey1, $primaryKey2)
    {
        $returnFlg          = true;
        $hissuErrFlg        = false;    //必須エラーフラグ
        $numericErrFlg      = false;    //数字エラーフラグ
        $lengthErrFlg       = false;    //レングスエラーフラグ
        $masterErrFlg       = false;    //マスタエラーフラグ
        $orderErrFlg        = false;    //固有エラーフラグ
        $duplicateCsvErrFlg = false;    //重複エラーフラグ
        $valuesErrFlg       = false;    //値不正エラーフラグ

        $hissuErrMsg        = "";       //必須エラーメッセージ
        $numericErrMsg      = "";       //数字エラーメッセージ
        $lengthErrMsg       = "";       //レングスエラーメッセージ
        $masterErrMsg       = "";       //マスタエラーメッセージ
        $orderErrMsg        = "";       //固有エラーメッセージ
        $duplicateCsvErrMsg = "";       //重複エラーメッセージ
        $valuesErrMsg       = "";       //値不正エラーメッセージ

        $hissuSep        = "";
        $numericSep      = "";
        $lengthSep       = "";
        $masterSep       = "";
        $orderSep        = "";
        $duplicateCsvSep = "";
        $valuesSep       = "";

        $viewcd = $record_div = $viewname1 = $viewname2 = $viewname3 = array();
        for ($i = 1; $i <= $col_no; $i++) {
            $viewcd[] = "VIEWCD".$i;
            $record_div[] = "RECORD_DIV".$i;
            $viewname1[] = "VIEWNAME1".$i;
            $viewname2[] = "VIEWNAME2".$i;
            //学期チェック
            if ($this->getSemesterCount > 2) {
                $viewname3[] = "VIEWNAME3".$i;
            }
        }
        $dupFlg1 = $this->duplicateCsvCheck($duplicateCsv1, $primaryKey1, $data);
        if ($dupFlg1[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複1(". $dupFlg1[1] ."行目)");
        }

        $dupFlg2 = $this->duplicateCsvCheck($duplicateCsv2, $primaryKey2, $data);
        if ($dupFlg2[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複2(". $dupFlg2[1] ."行目)");
        }

        //必須項目チェック
        if (!$col_no) {
            $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, "観点コード");
        }
        foreach ($checkdata as $key => $val) {
            //必須項目チェック
            if (in_array($key, array_merge(array("GRADE","CLASSCD","CURRICULUM_CD","SUBCLASSCD"), $viewcd, $record_div))) {
                if (!strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                }
            }

            //数値チェック
            if (in_array($key, array_merge(array("GRADE","CLASSCD","CURRICULUM_CD","SUBCLASSCD"), $viewcd, $record_div))) {
                if ($data[$key] != '' && !is_numeric($data[$key])) {
                    $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                }
            }

            //文字数チェック
            if (strlen($data[$key]) > $val[0]) {
                $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
            }

            //存在チェック
            if ($key == "CLASSCD") {
                if ($data["CLASSCD"] > '90') {
                    $returnFlg = $this->errSet($valuesErrFlg, $valuesErrMsg, "※値不正:", $valuesSep, "教科コードが90より大きい");
                }
            }
            if (in_array($key, array_merge($viewcd))) {
                if (substr($data[$key], 0, 2) != $data["CLASSCD"]) {
                    $returnFlg = $this->errSet($orderErrFlg, $orderErrMsg, "※固有エラー:", $orderSep, $val[1]."と教科コードが不一致");
                }
            }
        }

        //存在チェック
        $chk_data = array();
        $chk_data[] = array("QUERY"    => knjz401p_2Query::checkGrade($data),
                            "MSG"      => "学年");
        $chk_data[] = array("QUERY"    => knjz401p_2Query::checkSubclasscd($this, $data),
                            "MSG"      => "科目コード");

        for ($i=0; $i < get_count($chk_data); $i++) {
            if ($db->getOne($chk_data[$i]["QUERY"]) == "0") {
                $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, $chk_data[$i]["MSG"]);
            }
        }
        if (substr($data["SUBCLASSCD"], 0, 2) != $data["CLASSCD"]) {
            $returnFlg = $this->errSet($orderErrFlg, $orderErrMsg, "※固有エラー:", $orderSep, "科目コードの先頭２桁と教科コードが不一致");
        }

        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$hissuErrMsg.$numericErrMsg.$lengthErrMsg.$masterErrMsg.$valuesErrMsg.$orderErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knjz401p_2Query::insertQueryErr($db, $line, $checkError);
        }

        return $returnFlg;
    }

    //データの整合性チェック
    public function isDataMatched(&$data_arr)
    {
        //５千件を超えたら制限エラー
        if (get_count($data_arr) > 5000) {
            $this->setWarning("MSG915", "処理件数を５千件以下にして下さい 処理件数：".get_count($data_arr)."件");
            return false;
        }

        return true;
    }
}
