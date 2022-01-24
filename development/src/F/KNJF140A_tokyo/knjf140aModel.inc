<?php

require_once('for_php7.php');

class knjf140aModel extends Model {
    var $field    = array();
    var $data     = array();
    var $cmd;
    var $control; //コントロールマスタ
    var $staffcd; //登録者コード
    var $schoolCd;
    var $getPrgId;  //コールされたか
    var $sendAuth;  //親画面の権限
    var $auth;      //権限

    function knjf140aModel() {}

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
        $this->getPropertiesAll(); //プロパティファイル
        if (VARS::file("FILE")) {
            $this->field["FILE"] = VARS::file("FILE");        //ファイルのアップロード
            $this->field["FILE"]["name"] = i18n_convert($this->field["FILE"]["name"], "UTF-8", "SJIS-win");
        }
    }

    function &getMainModel() {}

    //ファイル取り込み
    function &getExecModel() {
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

        if (is_uploaded_file ($this->field["FILE"]["tmp_name"])) { //HTTP POSTによりアップロードされたファイルかどうかを調べる

            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            //エラーデータの削除
               $query = knjf140aQuery::deleteQueryErr();
            $result = $db->query($query);

            $record_no = 1;            //行番号(初期値)

            $fp = fopen ($this->field["FILE"]["tmp_name"], "r"); //ファイルまたはURLをオープンする
            while (!feof ($fp)) {                                //ファイルポインタがファイル終端に達しているかどうか調べる
                $buffer = fgets($fp, 4096);                      //ファイルポインタから1行取得する
                if (trim($buffer) != '') {
                    //１万件を超えたらループから抜ける。
                    if( $record_no > 10000 ) break;
                    //ヘッダ有なら１行目は無視
                    if( $this->field["HEADER"]=="on" && $record_no<2 ) {
                        $record_no++;
                        continue;
                    }
                    $buffer = i18n_convert($buffer,"UTF-8","SJIS-win");        //文字列を指定コードに変換する

                    $tmp = explode(",", $buffer);

                    if ($this->field["CSVCD"] == "1") { //一般
                        $data = array("CSVCD"                => trim($tmp[0]),
                                      "YEAR"                 => trim($tmp[1]),
                                      "DATE"                 => trim($tmp[2]),
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
                                      "TB_DATE"              => trim($tmp[41]),
                                      "TB_REACT"             => trim($tmp[42]),
                                      "TB_RESULT"            => trim($tmp[43]),
                                      "TB_BCGDATE"           => trim($tmp[44]),
                                      "TB_FILMDATE"          => trim($tmp[45]),
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
                                      "DOC_DATE"             => trim($tmp[59]),
                                      "TREATCD"              => trim($tmp[60]),
                                      "REMARK"               => trim($tmp[61]),
                                      "GUIDE_DIV"            => trim($tmp[62]),
                                      "JOINING_SPORTS_CLUB"  => trim($tmp[63]),
                                      "MEDICAL_HISTORY1"     => trim($tmp[64]),
                                      "MEDICAL_HISTORY2"     => trim($tmp[65]),
                                      "MEDICAL_HISTORY3"     => trim($tmp[66]),
                                      "DIAGNOSIS_NAME"       => trim($tmp[67]));
                    } else { //歯・口腔
                        if ($this->Properties["printKenkouSindanIppan"] == "2") {
                            $data = array("CSVCD"             => trim($tmp[0]),
                                          "YEAR"              => trim($tmp[1]),
                                          "DATE"              => trim($tmp[2]),
                                          "SCHREGNO"          => trim($tmp[3]),
                                          "JAWS_JOINTCD"      => trim($tmp[4]),
                                          "JAWS_JOINTCD3"     => trim($tmp[5]),
                                          "JAWS_JOINTCD2"     => trim($tmp[6]),
                                          "PLAQUECD"          => trim($tmp[7]),
                                          "GUMCD"             => trim($tmp[8]),
                                          "CALCULUS"          => trim($tmp[9]),
                                          "ORTHODONTICS"      => trim($tmp[10]),
                                          "UP_R_BABY5"        => trim($tmp[11]),
                                          "UP_R_BABY4"        => trim($tmp[12]),
                                          "UP_R_BABY3"        => trim($tmp[13]),
                                          "UP_R_BABY2"        => trim($tmp[14]),
                                          "UP_R_BABY1"        => trim($tmp[15]),
                                          "UP_L_BABY1"        => trim($tmp[16]),
                                          "UP_L_BABY2"        => trim($tmp[17]),
                                          "UP_L_BABY3"        => trim($tmp[18]),
                                          "UP_L_BABY4"        => trim($tmp[19]),
                                          "UP_L_BABY5"        => trim($tmp[20]),
                                          "LW_R_BABY5"        => trim($tmp[21]),
                                          "LW_R_BABY4"        => trim($tmp[22]),
                                          "LW_R_BABY3"        => trim($tmp[23]),
                                          "LW_R_BABY2"        => trim($tmp[24]),
                                          "LW_R_BABY1"        => trim($tmp[25]),
                                          "LW_L_BABY1"        => trim($tmp[26]),
                                          "LW_L_BABY2"        => trim($tmp[27]),
                                          "LW_L_BABY3"        => trim($tmp[28]),
                                          "LW_L_BABY4"        => trim($tmp[29]),
                                          "LW_L_BABY5"        => trim($tmp[30]),
                                          "BABYTOOTH"         => trim($tmp[31]),
                                          "REMAINBABYTOOTH"   => trim($tmp[32]),
                                          "TREATEDBABYTOOTH"  => trim($tmp[33]),
                                          "BRACK_BABYTOOTH"   => trim($tmp[34]),
                                          "UP_R_ADULT8"       => trim($tmp[35]),
                                          "UP_R_ADULT7"       => trim($tmp[36]),
                                          "UP_R_ADULT6"       => trim($tmp[37]),
                                          "UP_R_ADULT5"       => trim($tmp[38]),
                                          "UP_R_ADULT4"       => trim($tmp[39]),
                                          "UP_R_ADULT3"       => trim($tmp[40]),
                                          "UP_R_ADULT2"       => trim($tmp[41]),
                                          "UP_R_ADULT1"       => trim($tmp[42]),
                                          "UP_L_ADULT1"       => trim($tmp[43]),
                                          "UP_L_ADULT2"       => trim($tmp[44]),
                                          "UP_L_ADULT3"       => trim($tmp[45]),
                                          "UP_L_ADULT4"       => trim($tmp[46]),
                                          "UP_L_ADULT5"       => trim($tmp[47]),
                                          "UP_L_ADULT6"       => trim($tmp[48]),
                                          "UP_L_ADULT7"       => trim($tmp[49]),
                                          "UP_L_ADULT8"       => trim($tmp[50]),
                                          "LW_R_ADULT8"       => trim($tmp[51]),
                                          "LW_R_ADULT7"       => trim($tmp[52]),
                                          "LW_R_ADULT6"       => trim($tmp[53]),
                                          "LW_R_ADULT5"       => trim($tmp[54]),
                                          "LW_R_ADULT4"       => trim($tmp[55]),
                                          "LW_R_ADULT3"       => trim($tmp[56]),
                                          "LW_R_ADULT2"       => trim($tmp[57]),
                                          "LW_R_ADULT1"       => trim($tmp[58]),
                                          "LW_L_ADULT1"       => trim($tmp[59]),
                                          "LW_L_ADULT2"       => trim($tmp[60]),
                                          "LW_L_ADULT3"       => trim($tmp[61]),
                                          "LW_L_ADULT4"       => trim($tmp[62]),
                                          "LW_L_ADULT5"       => trim($tmp[63]),
                                          "LW_L_ADULT6"       => trim($tmp[64]),
                                          "LW_L_ADULT7"       => trim($tmp[65]),
                                          "LW_L_ADULT8"       => trim($tmp[66]),
                                          "ADULTTOOTH"        => trim($tmp[67]),
                                          "REMAINADULTTOOTH"  => trim($tmp[68]),
                                          "TREATEDADULTTOOTH" => trim($tmp[69]),
                                          "LOSTADULTTOOTH"    => trim($tmp[70]),
                                          "BRACK_ADULTTOOTH"  => trim($tmp[71]),
                                          "CHECKADULTTOOTH"   => trim($tmp[72]),
                                          "OTHERDISEASE"      => trim($tmp[73]),
                                          "DENTISTREMARK_CO"  => trim($tmp[74]),
                                          "DENTISTREMARK_GO"  => trim($tmp[75]),
                                          "DENTISTREMARK_G"   => trim($tmp[76]),
                                          "DENTISTREMARK"     => trim($tmp[77]),
                                          "DENTISTREMARKDATE" => trim($tmp[78]),
                                          "DENTISTTREAT"      => trim($tmp[79]));
                        } else {
                            $data = array("CSVCD"             => trim($tmp[0]),
                                          "YEAR"              => trim($tmp[1]),
                                          "DATE"              => trim($tmp[2]),
                                          "SCHREGNO"          => trim($tmp[3]),
                                          "JAWS_JOINTCD"      => trim($tmp[4]),
                                          "JAWS_JOINTCD2"     => trim($tmp[5]),
                                          "PLAQUECD"          => trim($tmp[6]),
                                          "GUMCD"             => trim($tmp[7]),
                                          "CALCULUS"          => trim($tmp[8]),
                                          "ORTHODONTICS"      => trim($tmp[9]),
                                          "UP_R_BABY5"        => trim($tmp[10]),
                                          "UP_R_BABY4"        => trim($tmp[11]),
                                          "UP_R_BABY3"        => trim($tmp[12]),
                                          "UP_R_BABY2"        => trim($tmp[13]),
                                          "UP_R_BABY1"        => trim($tmp[14]),
                                          "UP_L_BABY1"        => trim($tmp[15]),
                                          "UP_L_BABY2"        => trim($tmp[16]),
                                          "UP_L_BABY3"        => trim($tmp[17]),
                                          "UP_L_BABY4"        => trim($tmp[18]),
                                          "UP_L_BABY5"        => trim($tmp[19]),
                                          "LW_R_BABY5"        => trim($tmp[20]),
                                          "LW_R_BABY4"        => trim($tmp[21]),
                                          "LW_R_BABY3"        => trim($tmp[22]),
                                          "LW_R_BABY2"        => trim($tmp[23]),
                                          "LW_R_BABY1"        => trim($tmp[24]),
                                          "LW_L_BABY1"        => trim($tmp[25]),
                                          "LW_L_BABY2"        => trim($tmp[26]),
                                          "LW_L_BABY3"        => trim($tmp[27]),
                                          "LW_L_BABY4"        => trim($tmp[28]),
                                          "LW_L_BABY5"        => trim($tmp[29]),
                                          "BABYTOOTH"         => trim($tmp[30]),
                                          "REMAINBABYTOOTH"   => trim($tmp[31]),
                                          "TREATEDBABYTOOTH"  => trim($tmp[32]),
                                          "BRACK_BABYTOOTH"   => trim($tmp[33]),
                                          "UP_R_ADULT8"       => trim($tmp[34]),
                                          "UP_R_ADULT7"       => trim($tmp[35]),
                                          "UP_R_ADULT6"       => trim($tmp[36]),
                                          "UP_R_ADULT5"       => trim($tmp[37]),
                                          "UP_R_ADULT4"       => trim($tmp[38]),
                                          "UP_R_ADULT3"       => trim($tmp[39]),
                                          "UP_R_ADULT2"       => trim($tmp[40]),
                                          "UP_R_ADULT1"       => trim($tmp[41]),
                                          "UP_L_ADULT1"       => trim($tmp[42]),
                                          "UP_L_ADULT2"       => trim($tmp[43]),
                                          "UP_L_ADULT3"       => trim($tmp[44]),
                                          "UP_L_ADULT4"       => trim($tmp[45]),
                                          "UP_L_ADULT5"       => trim($tmp[46]),
                                          "UP_L_ADULT6"       => trim($tmp[47]),
                                          "UP_L_ADULT7"       => trim($tmp[48]),
                                          "UP_L_ADULT8"       => trim($tmp[49]),
                                          "LW_R_ADULT8"       => trim($tmp[50]),
                                          "LW_R_ADULT7"       => trim($tmp[51]),
                                          "LW_R_ADULT6"       => trim($tmp[52]),
                                          "LW_R_ADULT5"       => trim($tmp[53]),
                                          "LW_R_ADULT4"       => trim($tmp[54]),
                                          "LW_R_ADULT3"       => trim($tmp[55]),
                                          "LW_R_ADULT2"       => trim($tmp[56]),
                                          "LW_R_ADULT1"       => trim($tmp[57]),
                                          "LW_L_ADULT1"       => trim($tmp[58]),
                                          "LW_L_ADULT2"       => trim($tmp[59]),
                                          "LW_L_ADULT3"       => trim($tmp[60]),
                                          "LW_L_ADULT4"       => trim($tmp[61]),
                                          "LW_L_ADULT5"       => trim($tmp[62]),
                                          "LW_L_ADULT6"       => trim($tmp[63]),
                                          "LW_L_ADULT7"       => trim($tmp[64]),
                                          "LW_L_ADULT8"       => trim($tmp[65]),
                                          "ADULTTOOTH"        => trim($tmp[66]),
                                          "REMAINADULTTOOTH"  => trim($tmp[67]),
                                          "TREATEDADULTTOOTH" => trim($tmp[68]),
                                          "LOSTADULTTOOTH"    => trim($tmp[69]),
                                          "BRACK_ADULTTOOTH"  => trim($tmp[70]),
                                          "CHECKADULTTOOTH"   => trim($tmp[71]),
                                          "OTHERDISEASECD"    => trim($tmp[72]),
                                          "OTHERDISEASE"      => trim($tmp[73]),
                                          "DENTISTREMARKCD"   => trim($tmp[74]),
                                          "DENTISTREMARK"     => trim($tmp[75]),
                                          "DENTISTREMARKDATE" => trim($tmp[76]),
                                          "DENTISTTREATCD"    => trim($tmp[77]),
                                          "DENTISTTREAT"      => trim($tmp[78]));
                        }
                        $data["ORTHODONTICS"] = $data["ORTHODONTICS"] ? $data["ORTHODONTICS"] : 0; //矯正の有無(初期値を0とする)
                        $data["DENTISTREMARK_GO"] = $data["DENTISTREMARK_GO"] ? $data["DENTISTREMARK_GO"] : 0; //学校歯科医所見GOの有無(初期値を0とする)
                        $data["DENTISTREMARK_G"]  = $data["DENTISTREMARK_G"] ? $data["DENTISTREMARK_G"] : 0; //学校歯科医所見Gの有無(初期値を0とする)
                    }

                    $koumoku_error = $sonzai_error = "";    //エラー出力用

                    //対象データが異なる場合、ループから抜ける。
                    if ($this->field["CSVCD"] != $data["CSVCD"]) {
                        if ($this->field["CSVCD"] == "1") $koumoku_error = "CSVCDが健康診断（一般）ではありません。";
                        if ($this->field["CSVCD"] == "2") $koumoku_error = "CSVCDが健康診断（歯・口腔）ではありません。";
                    }

                    //項目チェック
                    $koumoku_error .= $this->koumoku_check($data, $db);

                    //存在チェック(項目チェックがＯＫの場合)
                    if($koumoku_error=="") {
                        $date_flg = true;
                        //日付変換
                        //一般-----
                        //歯・口腔-----
                        if ($data["DATE"]!="") {
                            $data_check = $this->date_rep($data["DATE"]);
                            if($data_check) {
                                $data["DATE"] = $data_check;
                            } else {
                                $date_flg = false;
                            }
                        }
                        //一般-----
                        if ($this->field["CSVCD"] == "1") {
                            if ($data["TB_DATE"]!="") {
                                $data_check = $this->date_rep($data["TB_DATE"]);
                                if($data_check)
                                {
                                    $data["TB_DATE"] = $data_check;
                                } else {
                                    $date_flg = false;
                                }
                            }
                            if ($data["TB_BCGDATE"]!="") {
                                $data_check = $this->date_rep($data["TB_BCGDATE"]);
                                if($data_check)
                                {
                                    $data["TB_BCGDATE"] = $data_check;
                                } else {
                                    $date_flg = false;
                                }
                            }
                            if ($data["TB_FILMDATE"]!="") {
                                $data_check = $this->date_rep($data["TB_FILMDATE"]);
                                if($data_check)
                                {
                                    $data["TB_FILMDATE"] = $data_check;
                                } else {
                                    $date_flg = false;
                                }
                            }
                            if ($data["DOC_DATE"]!="") {
                                $data_check = $this->date_rep($data["DOC_DATE"]);
                                if($data_check)
                                {
                                    $data["DOC_DATE"] = $data_check;
                                } else {
                                    $date_flg = false;
                                }
                            }
                        //歯・口腔-----
                        } else { 
                            if ($data["DENTISTREMARKDATE"]!="") {
                                $data_check = $this->date_rep($data["DENTISTREMARKDATE"]);
                                if($data_check) {
                                    $data["DENTISTREMARKDATE"] = $data_check;
                                } else {
                                    $date_flg = false;
                                }
                            }
                        }//csvcd
                        if( !$date_flg ) {$koumoku_error .= "日付エラー。";}

                        //０埋め
                        $data = $this->zero_ume($data);

                        //存在チェック(更新時のみ)
                        if($this->field["SHORI_MEI"]=="1") 
                            $sonzai_error = $this->sonzai_check($db,$data);
                    }

                    //取り込み処理（エラーの場合、エラーＤＢへの追加）
                    if($koumoku_error != "" || $sonzai_error != "") {
                        $check_error = "";
                        if ($this->field["CSVCD"] == "1") $check_error = "［一般］";
                        if ($this->field["CSVCD"] == "2") $check_error = "［歯・口腔］";
                        $check_error .= $koumoku_error .$sonzai_error;
                           knjf140aQuery::insertQueryErr($db, $record_no, $check_error);
                    } else {
                        //更新
                        if($this->field["SHORI_MEI"]=="1") {
                            //delete and insert
                            knjf140aQuery::deleteinsertQueryCsv($db, $data, $this->field["CSVCD"], $this);
                        //削除
                        } else {
                            //delete
                            knjf140aQuery::deleteQueryCsv($db, $data, $this->field["CSVCD"]);
                        }
                    }//ifの綴り
                }//ifの綴り
                $record_no++; //行番号(カウント)
                //１万件を超えたら強制終了する。
                if ($record_no > 10000) {
                    $this->message = "\\n制限(１万件)を超えました。\\n".$record_no."行目以降は、未登録です。";
                    break;
                }
            }//whileの綴り
            $db->commit(); // トランザクションをコミットする。
            Query::dbCheckIn($db);
            fclose($fp);
            $this->message .= "登録処理が終了したので、エラー確認してください。";
        } else {
            $this->warning = "ファイル名が不正です。";
        }
    }


    //ＣＳＶ出力
    function getDownloadModel() {
        //項目
        $header_err = array("MSGROW"            => "行番号",
                            "MSGREMARK"         => "メッセージ");
        if ($this->field["CSVCD"] == "1") { //一般
            $header = array("CSVCD"                  => "※CSVCD",
                            "YEAR"                   => "※年度",
                            "DATE"                   => "※健診実施日",
                            "SCHREGNO"               => "※学籍番号",
                            "HEIGHT"                 => "身長",
                            "WEIGHT"                 => "体重",
                            "SITHEIGHT"              => "座高",
                            "CHEST"                  => "胸囲",
                            "R_BAREVISION"           => "視力右裸眼(数字)",
                            "R_BAREVISION_MARK"      => "視力右裸眼(文字)",
                            "L_BAREVISION"           => "視力左裸眼(数字)",
                            "L_BAREVISION_MARK"      => "視力左裸眼(文字)",
                            "R_VISION"               => "視力右矯正(数字)",
                            "R_VISION_MARK"          => "視力右矯正(文字)",
                            "L_VISION"               => "視力左矯正(数字)",
                            "L_VISION_MARK"          => "視力左矯正(文字)",
                            "R_EAR"                  => "聴力右状態コード",
                            "R_EAR_DB"               => "聴力右DB",
                            "L_EAR"                  => "聴力左状態コード",
                            "L_EAR_DB"               => "聴力左DB",
                            "ALBUMINURIA1CD"         => "一次尿蛋白コード",
                            "URICSUGAR1CD"           => "一次尿糖コード",
                            "URICBLEED1CD"           => "一次尿潜血コード",
                            "ALBUMINURIA2CD"         => "二次尿蛋白コード",
                            "URICSUGAR2CD"           => "二次尿糖コード",
                            "URICBLEED2CD"           => "二次尿潜血コード",
                            "URICOTHERTEST"          => "尿その他の検査",
                            "URI_ADVISECD"           => "尿指導区分コード",
                            "NUTRITIONCD"            => "栄養状態コード",
                            "SPINERIBCD"             => "脊柱胸郭コード",
                            "SPINERIBCD_REMARK"      => "脊柱胸郭所見",
                            "EYEDISEASECD"           => "目疾病及異常コード",
                            "EYE_TEST_RESULT"        => "眼科所見",
                            "NOSEDISEASECD"          => "耳鼻咽頭疾患コード",
                            "NOSEDISEASECD_REMARK"   => "耳鼻咽頭疾患所見",
                            "SKINDISEASECD"          => "皮膚疾患コード",
                            "HEART_MEDEXAM"          => "心臓臨床医学的検査コード",
                            "HEART_MEDEXAM_REMARK"   => "心臓臨床医学的検査",
                            "HEARTDISEASECD"         => "心臓疾病及異常コード",
                            "HEARTDISEASECD_REMARK"  => "心臓疾病及異常所見",
                            "MANAGEMENT_DIV"         => "心臓管理区分",
                            "TB_DATE"                => "結核ツ反実施日",
                            "TB_REACT"               => "結核ツ反応",
                            "TB_RESULT"              => "結核ツ反判定コード",
                            "TB_BCGDATE"             => "結核BCG検査日",
                            "TB_FILMDATE"            => "結核撮影日付",
                            "TB_FILMNO"              => "結核フィルム番号",
                            "TB_REMARKCD"            => "結核所見コード",
                            "TB_OTHERTESTCD"         => "結核その他検査コード",
                            "TB_NAMECD"              => "結核病名コード",
                            "TB_ADVISECD"            => "結核指導区分コード",
                            "TB_X_RAY"               => "結核所見(X線)",
                            "ANEMIA_REMARK"          => "貧血所見",
                            "HEMOGLOBIN"             => "貧血ヘモグロビン値",
                            "PARASITE"               => "寄生虫卵",
                            "OTHERDISEASECD"         => "その他疾病及異常コード",
                            "OTHER_ADVISECD"         => "その他疾病指導区分コード",
                            "DOC_CD"                 => "学校医コード   ",
                            "DOC_REMARK"             => "学校医所見",
                            "DOC_DATE"               => "学校医日付",
                            "TREATCD"                => "事後処置コード",
                            "REMARK"                 => "備考",
                            "GUIDE_DIV"              => "指導区分",
                            "JOINING_SPORTS_CLUB"    => "運動部活動",
                            "MEDICAL_HISTORY1"       => "既往症1",
                            "MEDICAL_HISTORY2"       => "既往症2",
                            "MEDICAL_HISTORY3"       => "既往症3",
                            "DIAGNOSIS_NAME"         => "診断名",
                            "DUMMY"                  => "DUMMY");
            $header_rei = array("CSVCD"                 => "1",
                                "YEAR"                  => CTRL_YEAR,
                                "DATE"                  => CTRL_DATE,
                                "SCHREGNO"              => CTRL_YEAR."0001",
                                "HEIGHT"                => "165.5",
                                "WEIGHT"                => "80.5",
                                "SITHEIGHT"             => "75.5",
                                "CHEST"                 => "75.5",
                                "R_BAREVISION"          => "0.50",
                                "R_BAREVISION_MARK"     => "C",
                                "L_BAREVISION"          => "0.70",
                                "L_BAREVISION_MARK"     => "B",
                                "R_VISION"              => "1.50",
                                "R_VISION_MARK"         => "A",
                                "L_VISION"              => "1.70",
                                "L_VISION_MARK"         => "A",
                                "R_EAR"                 => "01",
                                "R_EAR_DB"              => "20",
                                "L_EAR"                 => "02",
                                "L_EAR_DB"              => "15",
                                "ALBUMINURIA1CD"        => "01",
                                "URICSUGAR1CD"          => "01",
                                "URICBLEED1CD"          => "01",
                                "ALBUMINURIA2CD"        => "01",
                                "URICSUGAR2CD"          => "01",
                                "URICBLEED2CD"          => "01",
                                "URICOTHERTEST"         => "再検査済　異常なし",
                                "URI_ADVISECD"          => "01",
                                "NUTRITIONCD"           => "01",
                                "SPINERIBCD"            => "01",
                                "SPINERIBCD_REMARK"     => "脊柱・胸郭所見",
                                "EYEDISEASECD"          => "01",
                                "EYE_TEST_RESULT"       => "目の疾病及び異常所見",
                                "NOSEDISEASECD"         => "01",
                                "NOSEDISEASECD_REMARK"  => "耳鼻咽頭疾患所見",
                                "SKINDISEASECD"         => "01",
                                "HEART_MEDEXAM"         => "01",
                                "HEART_MEDEXAM_REMARK"  => "不完全右脚ブロック、心雑音、不整脈など",
                                "HEARTDISEASECD"        => "01",
                                "HEARTDISEASECD_REMARK" => "疾病及び異常",
                                "MANAGEMENT_DIV"        => "01",
                                "TB_DATE"               => CTRL_DATE,
                                "TB_REACT"              => "20.0",
                                "TB_RESULT"             => "02",
                                "TB_BCGDATE"            => CTRL_DATE,
                                "TB_FILMDATE"           => CTRL_DATE,
                                "TB_FILMNO"             => "F55030",
                                "TB_REMARKCD"           => "01",
                                "TB_OTHERTESTCD"        => "01",
                                "TB_NAMECD"             => "01",
                                "TB_ADVISECD"           => "01",
                                "TB_X_RAY"              => "X線所見",
                                "ANEMIA_REMARK"         => "食事療法",
                                "HEMOGLOBIN"            => "10.5",
                                "PARASITE"              => "01",
                                "OTHERDISEASECD"        => "01",
                                "OTHER_ADVISECD"        => "01",
                                "DOC_CD"                => "01",
                                "DOC_REMARK"            => "食事療法",
                                "DOC_DATE"              => CTRL_DATE,
                                "TREATCD"               => "01",
                                "REMARK"                => "食事療法",
                                "GUIDE_DIV"             => "1",
                                "JOINING_SPORTS_CLUB"   => "1",
                                "MEDICAL_HISTORY1"      => "1",
                                "MEDICAL_HISTORY2"      => "2",
                                "MEDICAL_HISTORY3"      => "3",
                                "DIAGNOSIS_NAME"        => "診断名",
                                "DUMMY"                 => "DUMMY");
        } else { //歯・口腔
            if ($this->Properties["printKenkouSindanIppan"] == "2") {
                $header = array("CSVCD"             => "※CSVCD",
                                "YEAR"              => "※年度",
                                "DATE"              => "※健診実施日",
                                "SCHREGNO"          => "※学籍番号",
                                "JAWS_JOINTCD"      => "歯列顎関節コード",
                                "JAWS_JOINTCD3"     => "咬合顎関節コード",
                                "JAWS_JOINTCD2"     => "顎関節コード",
                                "PLAQUECD"          => "歯垢状態コード",
                                "GUMCD"             => "歯肉状態コード",
                                "CALCULUS"          => "歯石沈着",
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
                                "CHECKADULTTOOTH"   => "要精検歯数",
                                "OTHERDISEASE"      => "その他疾病及び異常",
                                "DENTISTREMARK_CO"  => "学校歯科医所見 CO",
                                "DENTISTREMARK_GO"  => "学校歯科医所見 GO",
                                "DENTISTREMARK_G"   => "学校歯科医所見 G",
                                "DENTISTREMARK"     => "学校医処理",
                                "DENTISTREMARKDATE" => "学校歯科医所見日付",
                                "DENTISTTREAT"      => "学校歯科医事後処置",
                                "DUMMY"             => "DUMMY");
                                
            $header_rei = array("CSVCD"             => "2",
                                "YEAR"              => CTRL_YEAR,
                                "DATE"              => CTRL_DATE,
                                "SCHREGNO"          => CTRL_YEAR."0001",
                                "JAWS_JOINTCD"      => "01",
                                "JAWS_JOINTCD3"     => "01",
                                "JAWS_JOINTCD2"     => "01",
                                "PLAQUECD"          => "02",
                                "GUMCD"             => "01",
                                "CALCULUS"          => "01",
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
                                "CHECKADULTTOOTH"   => "1",
                                "OTHERDISEASE"      => "その他疾病及び異常",
                                "DENTISTREMARK_CO"  => "99",
                                "DENTISTREMARK_GO"  => "1",
                                "DENTISTREMARK_G"   => "1",
                                "DENTISTREMARK"     => "未使用です",
                                "DENTISTREMARKDATE" => CTRL_DATE,
                                "DENTISTTREAT"      => "治療完了",
                                "DUMMY"             => "DUMMY");
                                
            } else {
                $header = array("CSVCD"             => "※CSVCD",
                                "YEAR"              => "※年度",
                                "DATE"              => "※健診実施日",
                                "SCHREGNO"          => "※学籍番号",
                                "JAWS_JOINTCD"      => "歯列咬合顎関節コード",
                                "JAWS_JOINTCD2"     => "顎関節コード",
                                "PLAQUECD"          => "歯垢状態コード",
                                "GUMCD"             => "歯肉状態コード",
                                "CALCULUS"          => "歯石沈着",
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
                                "CHECKADULTTOOTH"   => "要精検歯数",
                                "OTHERDISEASECD"    => "その他疾病及び異常コード",
                                "OTHERDISEASE"      => "その他疾病及び異常",
                                "DENTISTREMARKCD"   => "学校歯科医所見コード",
                                "DENTISTREMARK"     => "学校医処理",
                                "DENTISTREMARKDATE" => "学校歯科医所見日付",
                                "DENTISTTREATCD"    => "学校歯科医事後処置コード",
                                "DENTISTTREAT"      => "学校歯科医事後処置",
                                "DUMMY"             => "DUMMY");
                                
                $header_rei = array("CSVCD"             => "2",
                                    "YEAR"              => CTRL_YEAR,
                                    "DATE"              => CTRL_DATE,
                                    "SCHREGNO"          => CTRL_YEAR."0001",
                                    "JAWS_JOINTCD"      => "01",
                                    "JAWS_JOINTCD2"     => "01",
                                    "PLAQUECD"          => "02",
                                    "GUMCD"             => "01",
                                    "CALCULUS"          => "01",
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
                                    "CHECKADULTTOOTH"   => "1",
                                    "OTHERDISEASECD"    => "02",
                                    "OTHERDISEASE"      => "全角20文字（コードが99のみ入力可）",
                                    "DENTISTREMARKCD"   => "02",
                                    "DENTISTREMARK"     => "未使用です",
                                    "DENTISTREMARKDATE" => CTRL_DATE,
                                    "DENTISTTREATCD"    => "01",
                                    "DENTISTTREAT"      => "治療完了",
                                    "DUMMY"             => "DUMMY");
            }
        }

        $contents  = ""; //クリア

        if($this->field["OUTPUT"]=="1") { //ヘッダ
            $contents .= implode($header, ",")."\n";
            $contents .= implode($header_rei, ",")."\n";
        } else if($this->field["OUTPUT"]=="3") {        //エラー
            $contents .= implode($header_err, ",")."\n";
        } else if($this->field["OUTPUT"]=="4") {        //データ
            if( $this->field["HEADER"]=="on" ) $contents .= implode($header, ",")."\n";
        }
        if($this->field["OUTPUT"]=="1") { //ヘッダ
            $header_data = true;
        } else { //ヘッダ以外
            $header_data = false;
            //DB接続
            $db = Query::dbCheckOut();
            if($this->field["OUTPUT"]=="3") { //エラー
                $query = knjf140aQuery::selectCsvQuery();
            } else if($this->field["OUTPUT"]=="4") { //データ
                $query = knjf140aQuery::getselectDataSQL($this);
            }
            //SQL文発行
            $result = $db->query($query);
            while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if($this->field["OUTPUT"]=="4") { //データ
                    $row = str_replace(",",".",$row);//データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                }
                $contents .= implode($row, ",") ."\n";
                $header_data = true;
            }
            $result->free();
            Query::dbCheckIn($db);
        }
        //ＣＳＶ出力
        if($header_data) {
            if($this->field["OUTPUT"]=="3") {        //エラー
                $couse = "エラー出力";
            } else {                                //エラー以外
                $couse = ($this->field["CSVCD"] == "1") ? "健康診断（一般）" : "健康診断（歯・口腔）";
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

    /**
     * 項目チェック
     */
    function koumoku_check(&$data, $db) {
        $koumoku_error = "";        //項目エラー用
        //更新
        if($this->field["SHORI_MEI"]=="1") {
            //一般-----
            if ($this->field["CSVCD"] == "1") {
                //桁数チェック
                if (strlen($data["YEAR"])                  > 4   || 
                    strlen($data["DATE"])                  > 10  || 
                    strlen($data["SCHREGNO"])              > 8   || 
                    strlen($data["HEIGHT"])                > 5   || 
                    strlen($data["WEIGHT"])                > 5   || 
                    strlen($data["SITHEIGHT"])             > 5   || 
                    strlen($data["CHEST"])                 > 5   || 
                    strlen($data["R_BAREVISION"])          > 4   || 
                    strlen($data["R_BAREVISION_MARK"])     > 3   || 
                    strlen($data["L_BAREVISION"])          > 4   || 
                    strlen($data["L_BAREVISION_MARK"])     > 3   || 
                    strlen($data["R_VISION"])              > 4   || 
                    strlen($data["R_VISION_MARK"])         > 3   || 
                    strlen($data["L_VISION"])              > 4   || 
                    strlen($data["L_VISION_MARK"])         > 3   || 
                    strlen($data["R_EAR"])                 > 2   || 
                    strlen($data["R_EAR_DB"])              > 3   || 
                    strlen($data["L_EAR"])                 > 2   || 
                    strlen($data["L_EAR_DB"])              > 3   || 
                    strlen($data["ALBUMINURIA1CD"])        > 2   || 
                    strlen($data["URICSUGAR1CD"])          > 2   || 
                    strlen($data["URICBLEED1CD"])          > 2   || 
                    strlen($data["ALBUMINURIA2CD"])        > 2   || 
                    strlen($data["URICSUGAR2CD"])          > 2   || 
                    strlen($data["URICBLEED2CD"])          > 2   || 
                    strlen($data["URICOTHERTEST"])         > 60  || 
                    strlen($data["URI_ADVISECD"])          > 2   || 
                    strlen($data["NUTRITIONCD"])           > 2   || 
                    strlen($data["SPINERIBCD"])            > 2   || 
                    strlen($data["SPINERIBCD_REMARK"])     > 60  || 
                    strlen($data["EYEDISEASECD"])          > 2   || 
                    strlen($data["EYE_TEST_RESULT"])       > 60  || 
                    strlen($data["NOSEDISEASECD"])         > 2   || 
                    strlen($data["NOSEDISEASECD_REMARK"])  > 60  || 
                    strlen($data["SKINDISEASECD"])         > 2   || 
                    strlen($data["HEART_MEDEXAM"])         > 2   || 
                    strlen($data["HEART_MEDEXAM_REMARK"])  > 120 || 
                    strlen($data["HEARTDISEASECD"])        > 2   || 
                    strlen($data["HEARTDISEASECD_REMARK"]) > 60  || 
                    strlen($data["MANAGEMENT_DIV"])        > 2   || 
                    strlen($data["TB_DATE"])               > 10  || 
                    strlen($data["TB_REACT"])              > 4   || 
                    strlen($data["TB_RESULT"])             > 2   || 
                    strlen($data["TB_BCGDATE"])            > 10  || 
                    strlen($data["TB_FILMDATE"])           > 10  || 
                    strlen($data["TB_FILMNO"])             > 6   || 
                    strlen($data["TB_REMARKCD"])           > 2   || 
                    strlen($data["TB_OTHERTESTCD"])        > 2   || 
                    strlen($data["TB_NAMECD"])             > 2   || 
                    strlen($data["TB_ADVISECD"])           > 2   || 
                    strlen($data["TB_X_RAY"])              > 60  || 
                    strlen($data["ANEMIA_REMARK"])         > 30  || 
                    strlen($data["HEMOGLOBIN"])            > 4   || 
                    strlen($data["PARASITE"])              > 2   || 
                    strlen($data["OTHERDISEASECD"])        > 2   || 
                    strlen($data["OTHER_ADVISECD"])        > 2   || 
                    strlen($data["DOC_CD"])                > 2   || 
                    strlen($data["DOC_REMARK"])            > 60  || 
                    strlen($data["DOC_DATE"])              > 10  || 
                    strlen($data["TREATCD"])               > 2   || 
                    strlen($data["REMARK"])                > 180 || 
                    strlen($data["GUIDE_DIV"])             > 1   || 
                    strlen($data["JOINING_SPORTS_CLUB"])   > 1   || 
                    strlen($data["MEDICAL_HISTORY1"])      > 3   || 
                    strlen($data["MEDICAL_HISTORY2"])      > 3   || 
                    strlen($data["MEDICAL_HISTORY3"])      > 3   || 
                    strlen($data["DIAGNOSIS_NAME"])        > 150)
                {
                    $koumoku_error .= "桁数エラー。";
                }
                //必須項目チェック
                if ($data["YEAR"]=="" || $data["DATE"]=="" || $data["SCHREGNO"]=="") {
                    $koumoku_error .= "必須項目エラー。";
                }
                //数字チェック
                if (!$this->suuji_check($data["YEAR"]) || 
                    !$this->suuji_check($data["SCHREGNO"]) || 
                    !$this->suuji_check(str_replace(".","",$data["HEIGHT"])) || 
                    !$this->suuji_check(str_replace(".","",$data["WEIGHT"])) || 
                    !$this->suuji_check(str_replace(".","",$data["SITHEIGHT"])) || 
                    !$this->suuji_check(str_replace(".","",$data["CHEST"])) || 
                    !$this->suuji_check(str_replace(".","",$data["R_BAREVISION"])) || 
                    !$this->suuji_check(str_replace(".","",$data["L_BAREVISION"])) || 
                    !$this->suuji_check(str_replace(".","",$data["R_VISION"])) || 
                    !$this->suuji_check(str_replace(".","",$data["L_VISION"])) || 
                    !$this->suuji_check($data["R_EAR"]) || 
                    !$this->suuji_check($data["R_EAR_DB"]) || 
                    !$this->suuji_check($data["L_EAR"]) || 
                    !$this->suuji_check($data["L_EAR_DB"]) || 
                    !$this->suuji_check($data["ALBUMINURIA1CD"]) || 
                    !$this->suuji_check($data["URICSUGAR1CD"]) || 
                    !$this->suuji_check($data["URICBLEED1CD"]) || 
                    !$this->suuji_check($data["ALBUMINURIA2CD"]) || 
                    !$this->suuji_check($data["URICSUGAR2CD"]) || 
                    !$this->suuji_check($data["URICBLEED2CD"]) || 
                    !$this->suuji_check($data["URI_ADVISECD"]) || 
                    !$this->suuji_check($data["NUTRITIONCD"]) || 
                    !$this->suuji_check($data["SPINERIBCD"]) || 
                    !$this->suuji_check($data["EYEDISEASECD"]) || 
                    !$this->suuji_check($data["NOSEDISEASECD"]) || 
                    !$this->suuji_check($data["SKINDISEASECD"]) || 
                    !$this->suuji_check($data["HEART_MEDEXAM"]) || 
                    !$this->suuji_check($data["HEARTDISEASECD"]) || 
                    !$this->suuji_check($data["MANAGEMENT_DIV"]) || 
                    !$this->suuji_check(str_replace(".","",$data["TB_REACT"])) || 
                    !$this->suuji_check($data["TB_RESULT"]) || 
                    !$this->suuji_check($data["TB_REMARKCD"]) || 
                    !$this->suuji_check($data["TB_OTHERTESTCD"]) || 
                    !$this->suuji_check($data["TB_NAMECD"]) || 
                    !$this->suuji_check($data["TB_ADVISECD"]) || 
                    !$this->suuji_check(str_replace(".","",$data["HEMOGLOBIN"])) || 
                    !$this->suuji_check($data["PARASITE"]) || 
                    !$this->suuji_check($data["OTHERDISEASECD"]) || 
                    !$this->suuji_check($data["OTHER_ADVISECD"]) || 
                    !$this->suuji_check($data["DOC_CD"]) || 
                    !$this->suuji_check($data["TREATCD"]) ||
                    !$this->suuji_check($data["GUIDE_DIV"]) ||
                    !$this->suuji_check($data["JOINING_SPORTS_CLUB"]) ||
                    !$this->suuji_check($data["MEDICAL_HISTORY1"]) ||
                    !$this->suuji_check($data["MEDICAL_HISTORY2"]) ||
                    !$this->suuji_check($data["MEDICAL_HISTORY3"]) )
                {
                       $koumoku_error .= "数字項目エラー。";
                }
                //値チェック
                if (!$this->atai_check($data["HEIGHT"],3,1) || 
                    !$this->atai_check($data["WEIGHT"],3,1) || 
                    !$this->atai_check($data["SITHEIGHT"],3,1) || 
                    !$this->atai_check($data["CHEST"],3,1) || 
                    !$this->atai_check($data["R_BAREVISION"],1,2) || 
                    !$this->atai_check($data["L_BAREVISION"],1,2) || 
                    !$this->atai_check($data["R_VISION"],1,2) || 
                    !$this->atai_check($data["L_VISION"],1,2) || 
                    !$this->atai_check($data["TB_REACT"],2,1) || 
                    !$this->atai_check($data["HEMOGLOBIN"],2,1) )
                {
                       $koumoku_error .= "値エラー。";
                }
                //視力の文字チェック
                $koumoku_error .= $this->siryokumoji_check($data["R_BAREVISION_MARK"], $data["R_BAREVISION"], $db);
                $koumoku_error .= $this->siryokumoji_check($data["L_BAREVISION_MARK"], $data["L_BAREVISION"], $db);
                $koumoku_error .= $this->siryokumoji_check($data["R_VISION_MARK"],     $data["R_VISION"], $db);
                $koumoku_error .= $this->siryokumoji_check($data["L_VISION_MARK"],     $data["L_VISION"], $db);
            //歯・口腔-----
            } else {
                //桁数チェック
                if (strlen($data["YEAR"]) > 4 || 
                    strlen($data["DATE"]) > 10 || 
                    strlen($data["SCHREGNO"]) > 8 || 
                    strlen($data["JAWS_JOINTCD"]) > 2 || 
                    strlen($data["JAWS_JOINTCD3"]) > 2 || //熊本仕様
                    strlen($data["JAWS_JOINTCD2"]) > 2 || 
                    strlen($data["PLAQUECD"]) > 2 || 
                    strlen($data["GUMCD"]) > 2 || 
                    strlen($data["CALCULUS"]) > 2 || 
                    strlen($data["ORTHODONTICS"]) > 1 || 
                    strlen($data["UP_R_BABY5"]) > 2 || 
                    strlen($data["UP_R_BABY4"]) > 2 || 
                    strlen($data["UP_R_BABY3"]) > 2 || 
                    strlen($data["UP_R_BABY2"]) > 2 || 
                    strlen($data["UP_R_BABY1"]) > 2 || 
                    strlen($data["UP_L_BABY1"]) > 2 || 
                    strlen($data["UP_L_BABY2"]) > 2 || 
                    strlen($data["UP_L_BABY3"]) > 2 || 
                    strlen($data["UP_L_BABY4"]) > 2 || 
                    strlen($data["UP_L_BABY5"]) > 2 || 
                    strlen($data["LW_R_BABY5"]) > 2 || 
                    strlen($data["LW_R_BABY4"]) > 2 || 
                    strlen($data["LW_R_BABY3"]) > 2 || 
                    strlen($data["LW_R_BABY2"]) > 2 || 
                    strlen($data["LW_R_BABY1"]) > 2 || 
                    strlen($data["LW_L_BABY1"]) > 2 || 
                    strlen($data["LW_L_BABY2"]) > 2 || 
                    strlen($data["LW_L_BABY3"]) > 2 || 
                    strlen($data["LW_L_BABY4"]) > 2 || 
                    strlen($data["LW_L_BABY5"]) > 2 || 
                    strlen($data["BABYTOOTH"]) > 2 || 
                    strlen($data["REMAINBABYTOOTH"]) > 2 || 
                    strlen($data["TREATEDBABYTOOTH"]) > 2 || 
                    strlen($data["BRACK_BABYTOOTH"]) > 2 || 
                    strlen($data["UP_R_ADULT8"]) > 2 || 
                    strlen($data["UP_R_ADULT7"]) > 2 || 
                    strlen($data["UP_R_ADULT6"]) > 2 || 
                    strlen($data["UP_R_ADULT5"]) > 2 || 
                    strlen($data["UP_R_ADULT4"]) > 2 || 
                    strlen($data["UP_R_ADULT3"]) > 2 || 
                    strlen($data["UP_R_ADULT2"]) > 2 || 
                    strlen($data["UP_R_ADULT1"]) > 2 || 
                    strlen($data["UP_L_ADULT1"]) > 2 || 
                    strlen($data["UP_L_ADULT2"]) > 2 || 
                    strlen($data["UP_L_ADULT3"]) > 2 || 
                    strlen($data["UP_L_ADULT4"]) > 2 || 
                    strlen($data["UP_L_ADULT5"]) > 2 || 
                    strlen($data["UP_L_ADULT6"]) > 2 || 
                    strlen($data["UP_L_ADULT7"]) > 2 || 
                    strlen($data["UP_L_ADULT8"]) > 2 || 
                    strlen($data["LW_R_ADULT8"]) > 2 || 
                    strlen($data["LW_R_ADULT7"]) > 2 || 
                    strlen($data["LW_R_ADULT6"]) > 2 || 
                    strlen($data["LW_R_ADULT5"]) > 2 || 
                    strlen($data["LW_R_ADULT4"]) > 2 || 
                    strlen($data["LW_R_ADULT3"]) > 2 || 
                    strlen($data["LW_R_ADULT2"]) > 2 || 
                    strlen($data["LW_R_ADULT1"]) > 2 || 
                    strlen($data["LW_L_ADULT1"]) > 2 || 
                    strlen($data["LW_L_ADULT2"]) > 2 || 
                    strlen($data["LW_L_ADULT3"]) > 2 || 
                    strlen($data["LW_L_ADULT4"]) > 2 || 
                    strlen($data["LW_L_ADULT5"]) > 2 || 
                    strlen($data["LW_L_ADULT6"]) > 2 || 
                    strlen($data["LW_L_ADULT7"]) > 2 || 
                    strlen($data["LW_L_ADULT8"]) > 2 || 
                    strlen($data["ADULTTOOTH"]) > 2 || 
                    strlen($data["REMAINADULTTOOTH"]) > 2 || 
                    strlen($data["TREATEDADULTTOOTH"]) > 2 || 
                    strlen($data["LOSTADULTTOOTH"]) > 2 || 
                    strlen($data["BRACK_ADULTTOOTH"]) > 2 || 
                    strlen($data["CHECKADULTTOOTH"]) > 2 || 
                    strlen($data["OTHERDISEASECD"]) > 2 || 
                    strlen($data["OTHERDISEASE"]) > 60 || 
                    //熊本仕様のみ
                    strlen($data["DENTISTREMARK_CO"])  > 2 || 
                    strlen($data["DENTISTREMARK_GO"])  > 1 || 
                    strlen($data["DENTISTREMARK_G"])  > 1 || 
                    //ここまで
                    strlen($data["DENTISTREMARKCD"]) > 2 || 
                    strlen($data["DENTISTREMARK"]) > 30 || 
                    strlen($data["DENTISTREMARKDATE"]) > 10 || 
                    strlen($data["DENTISTTREATCD"]) > 2 || 
                    strlen($data["DENTISTTREAT"]) > 30)
                {
                    $koumoku_error .= "桁数エラー。";
                }
                //必須項目チェック
                if ($data["YEAR"]=="" || 
                    $data["DATE"]=="" || 
                    $data["SCHREGNO"]=="")
                {
                    $koumoku_error .= "必須項目エラー。";
                }
                if ($data["ORTHODONTICS"] != "1" && $data["ORTHODONTICS"] != "0") {
                    $koumoku_error .= "矯正の有無エラー";
                }
                //熊本仕様
                if ($data["DENTISTREMARK_GO"] != "1" && $data["DENTISTREMARK_GO"] != "0") {
                    $koumoku_error .= "学校歯科医所見 GOの有無エラー";
                }
                if ($data["DENTISTREMARK_G"] != "1" && $data["DENTISTREMARK_G"] != "0") {
                    $koumoku_error .= "学校歯科医所見 Gの有無エラー";
                }
                
                //数字チェック
                if (!$this->suuji_check($data["YEAR"]) || 
                    !$this->suuji_check($data["SCHREGNO"]) || 
                    !$this->suuji_check($data["JAWS_JOINTCD"]) || 
                    !$this->suuji_check($data["JAWS_JOINTCD3"]) || //熊本仕様
                    !$this->suuji_check($data["JAWS_JOINTCD2"]) || 
                    !$this->suuji_check($data["PLAQUECD"]) || 
                    !$this->suuji_check($data["GUMCD"]) || 
                    !$this->suuji_check($data["CALCULUS"]) || 
                    !$this->suuji_check($data["ORTHODONTICS"]) || 
                    !$this->suuji_check($data["UP_R_BABY5"]) || 
                    !$this->suuji_check($data["UP_R_BABY4"]) || 
                    !$this->suuji_check($data["UP_R_BABY3"]) || 
                    !$this->suuji_check($data["UP_R_BABY2"]) || 
                    !$this->suuji_check($data["UP_R_BABY1"]) || 
                    !$this->suuji_check($data["UP_L_BABY1"]) || 
                    !$this->suuji_check($data["UP_L_BABY2"]) || 
                    !$this->suuji_check($data["UP_L_BABY3"]) || 
                    !$this->suuji_check($data["UP_L_BABY4"]) || 
                    !$this->suuji_check($data["UP_L_BABY5"]) || 
                    !$this->suuji_check($data["LW_R_BABY5"]) || 
                    !$this->suuji_check($data["LW_R_BABY4"]) || 
                    !$this->suuji_check($data["LW_R_BABY3"]) || 
                    !$this->suuji_check($data["LW_R_BABY2"]) || 
                    !$this->suuji_check($data["LW_R_BABY1"]) || 
                    !$this->suuji_check($data["LW_L_BABY1"]) || 
                    !$this->suuji_check($data["LW_L_BABY2"]) || 
                    !$this->suuji_check($data["LW_L_BABY3"]) || 
                    !$this->suuji_check($data["LW_L_BABY4"]) || 
                    !$this->suuji_check($data["LW_L_BABY5"]) || 
                    !$this->suuji_check($data["BABYTOOTH"]) || 
                    !$this->suuji_check($data["REMAINBABYTOOTH"]) || 
                    !$this->suuji_check($data["TREATEDBABYTOOTH"]) || 
                    !$this->suuji_check($data["BRACK_BABYTOOTH"]) || 
                    !$this->suuji_check($data["UP_R_ADULT8"]) || 
                    !$this->suuji_check($data["UP_R_ADULT7"]) || 
                    !$this->suuji_check($data["UP_R_ADULT6"]) || 
                    !$this->suuji_check($data["UP_R_ADULT5"]) || 
                    !$this->suuji_check($data["UP_R_ADULT4"]) || 
                    !$this->suuji_check($data["UP_R_ADULT3"]) || 
                    !$this->suuji_check($data["UP_R_ADULT2"]) || 
                    !$this->suuji_check($data["UP_R_ADULT1"]) || 
                    !$this->suuji_check($data["UP_L_ADULT1"]) || 
                    !$this->suuji_check($data["UP_L_ADULT2"]) || 
                    !$this->suuji_check($data["UP_L_ADULT3"]) || 
                    !$this->suuji_check($data["UP_L_ADULT4"]) || 
                    !$this->suuji_check($data["UP_L_ADULT5"]) || 
                    !$this->suuji_check($data["UP_L_ADULT6"]) || 
                    !$this->suuji_check($data["UP_L_ADULT7"]) || 
                    !$this->suuji_check($data["UP_L_ADULT8"]) || 
                    !$this->suuji_check($data["LW_R_ADULT8"]) || 
                    !$this->suuji_check($data["LW_R_ADULT7"]) || 
                    !$this->suuji_check($data["LW_R_ADULT6"]) || 
                    !$this->suuji_check($data["LW_R_ADULT5"]) || 
                    !$this->suuji_check($data["LW_R_ADULT4"]) || 
                    !$this->suuji_check($data["LW_R_ADULT3"]) || 
                    !$this->suuji_check($data["LW_R_ADULT2"]) || 
                    !$this->suuji_check($data["LW_R_ADULT1"]) || 
                    !$this->suuji_check($data["LW_L_ADULT1"]) || 
                    !$this->suuji_check($data["LW_L_ADULT2"]) || 
                    !$this->suuji_check($data["LW_L_ADULT3"]) || 
                    !$this->suuji_check($data["LW_L_ADULT4"]) || 
                    !$this->suuji_check($data["LW_L_ADULT5"]) || 
                    !$this->suuji_check($data["LW_L_ADULT6"]) || 
                    !$this->suuji_check($data["LW_L_ADULT7"]) || 
                    !$this->suuji_check($data["LW_L_ADULT8"]) || 
                    !$this->suuji_check($data["ADULTTOOTH"]) || 
                    !$this->suuji_check($data["REMAINADULTTOOTH"]) || 
                    !$this->suuji_check($data["TREATEDADULTTOOTH"]) || 
                    !$this->suuji_check($data["LOSTADULTTOOTH"]) || 
                    !$this->suuji_check($data["BRACK_ADULTTOOTH"]) || 
                    !$this->suuji_check($data["CHECKADULTTOOTH"]) || 
                    !$this->suuji_check($data["OTHERDISEASECD"]) || 
                    !$this->suuji_check($data["DENTISTREMARK_CO"]) || //熊本仕様
                    !$this->suuji_check($data["DENTISTREMARKCD"]) || 
                    !$this->suuji_check($data["DENTISTTREATCD"]) )
                {
                       $koumoku_error .= "数字項目エラー。";
                }
            }//csvcd
        //削除
        } else {
            //一般-----
            //歯・口腔-----
            //桁数チェック
            if (strlen($data["YEAR"]) > 4 || 
                strlen($data["DATE"]) > 10 || 
                strlen($data["SCHREGNO"]) > 8)
            {
                $koumoku_error .= "桁数エラー。";
            }
            //必須項目チェック
            if ($data["YEAR"]=="" || 
                $data["DATE"]=="" || 
                $data["SCHREGNO"]=="")
            {
                $koumoku_error .= "必須項目エラー。";
            }
            //数字チェック
            if (!$this->suuji_check($data["YEAR"]) || 
                !$this->suuji_check($data["SCHREGNO"]) )
            {
                   $koumoku_error .= "数字項目エラー。";
            }
        }
        return $koumoku_error;    //項目エラー用
    }

    //数字チェック
    function suuji_check($data) {
        $flg = true;

        if ( $data!="" ) {
            if ( !is_numeric($data) ) $flg = false;
        }
        return $flg;
    }

    //値チェック
    function atai_check($data,$atai1,$atai2) {
        $flg = true;

        if ( $data!="" ) {
            $data_arr = array();
            $data_arr = explode(".",$data);
            if (strlen($data_arr[0]) > $atai1) $flg = false;
            if (strlen($data_arr[1]) > $atai2) $flg = false;
        }

        return $flg;
    }

    /**
     * 日付変換 項目ﾁｪｯｸ後に行う。
     */
    function date_rep($data) {
        if ($data!="") {
            $data = common::DateConv1(str_replace("-","/",$data),4);
            if($data) $data = str_replace("/","-",$data);
        }

        return $data;
    }

    /**
     * ０埋め 項目ﾁｪｯｸ後に行う。
     */
    function zero_ume($data) {
        //更新
        if($this->field["SHORI_MEI"]=="1") {
            //一般-----
            //歯・口腔-----
            if ($data["SCHREGNO"]!="")
                if (strlen($data["SCHREGNO"]) < 8) $data["SCHREGNO"] = sprintf("%08d", $data["SCHREGNO"]);

            //一般-----
            if ($this->field["CSVCD"] == "1") {
                if ($data["R_BAREVISION"]!="") {
                    $data_arr = array();
                    $data_arr = explode(".",$data["R_BAREVISION"]);
                    $data["R_BAREVISION"] = $data_arr[0] . "." . sprintf("%-02s", $data_arr[1]);
                }
                if ($data["L_BAREVISION"]!="") {
                    $data_arr = array();
                    $data_arr = explode(".",$data["L_BAREVISION"]);
                    $data["L_BAREVISION"] = $data_arr[0] . "." . sprintf("%-02s", $data_arr[1]);
                }
                if ($data["R_VISION"]!="") {
                    $data_arr = array();
                    $data_arr = explode(".",$data["R_VISION"]);
                    $data["R_VISION"] = $data_arr[0] . "." . sprintf("%-02s", $data_arr[1]);
                }
                if ($data["L_VISION"]!="") {
                    $data_arr = array();
                    $data_arr = explode(".",$data["L_VISION"]);
                    $data["L_VISION"] = $data_arr[0] . "." . sprintf("%-02s", $data_arr[1]);
                }
                if ($data["R_EAR"]!="")
                    if (strlen($data["R_EAR"])          < 2) $data["R_EAR"]          = sprintf("%02d", $data["R_EAR"]);
                if ($data["L_EAR"]!="")
                    if (strlen($data["L_EAR"])          < 2) $data["L_EAR"]          = sprintf("%02d", $data["L_EAR"]);
                if ($data["ALBUMINURIA1CD"]!="")
                    if (strlen($data["ALBUMINURIA1CD"]) < 2) $data["ALBUMINURIA1CD"] = sprintf("%02d", $data["ALBUMINURIA1CD"]);
                if ($data["URICSUGAR1CD"]!="")
                    if (strlen($data["URICSUGAR1CD"])   < 2) $data["URICSUGAR1CD"]   = sprintf("%02d", $data["URICSUGAR1CD"]);
                if ($data["URICBLEED1CD"]!="")
                    if (strlen($data["URICBLEED1CD"])   < 2) $data["URICBLEED1CD"]   = sprintf("%02d", $data["URICBLEED1CD"]);
                if ($data["ALBUMINURIA2CD"]!="")
                    if (strlen($data["ALBUMINURIA2CD"]) < 2) $data["ALBUMINURIA2CD"] = sprintf("%02d", $data["ALBUMINURIA2CD"]);
                if ($data["URICSUGAR2CD"]!="")
                    if (strlen($data["URICSUGAR2CD"])   < 2) $data["URICSUGAR2CD"]   = sprintf("%02d", $data["URICSUGAR2CD"]);
                if ($data["URICBLEED2CD"]!="")
                    if (strlen($data["URICBLEED2CD"])   < 2) $data["URICBLEED2CD"]   = sprintf("%02d", $data["URICBLEED2CD"]);
                if ($data["URI_ADVISECD"]!="")
                    if (strlen($data["URI_ADVISECD"])   < 2) $data["URI_ADVISECD"]   = sprintf("%02d", $data["URI_ADVISECD"]);
                if ($data["NUTRITIONCD"]!="")
                    if (strlen($data["NUTRITIONCD"])    < 2) $data["NUTRITIONCD"]    = sprintf("%02d", $data["NUTRITIONCD"]);
                if ($data["SPINERIBCD"]!="")
                    if (strlen($data["SPINERIBCD"])     < 2) $data["SPINERIBCD"]     = sprintf("%02d", $data["SPINERIBCD"]);
                if ($data["EYEDISEASECD"]!="")
                    if (strlen($data["EYEDISEASECD"])   < 2) $data["EYEDISEASECD"]   = sprintf("%02d", $data["EYEDISEASECD"]);
                if ($data["NOSEDISEASECD"]!="")
                    if (strlen($data["NOSEDISEASECD"])  < 2) $data["NOSEDISEASECD"]  = sprintf("%02d", $data["NOSEDISEASECD"]);
                if ($data["SKINDISEASECD"]!="")
                    if (strlen($data["SKINDISEASECD"])  < 2) $data["SKINDISEASECD"]  = sprintf("%02d", $data["SKINDISEASECD"]);
                if ($data["HEART_MEDEXAM"]!="")
                    if (strlen($data["HEART_MEDEXAM"])  < 2) $data["HEART_MEDEXAM"]  = sprintf("%02d", $data["HEART_MEDEXAM"]);
                if ($data["HEARTDISEASECD"]!="")
                    if (strlen($data["HEARTDISEASECD"]) < 2) $data["HEARTDISEASECD"] = sprintf("%02d", $data["HEARTDISEASECD"]);
                if ($data["MANAGEMENT_DIV"]!="")
                    if (strlen($data["MANAGEMENT_DIV"]) < 2) $data["MANAGEMENT_DIV"] = sprintf("%02d", $data["MANAGEMENT_DIV"]);
                if ($data["TB_RESULT"]!="")
                    if (strlen($data["TB_RESULT"])      < 2) $data["TB_RESULT"]      = sprintf("%02d", $data["TB_RESULT"]);
                if ($data["TB_REMARKCD"]!="")
                    if (strlen($data["TB_REMARKCD"])    < 2) $data["TB_REMARKCD"]    = sprintf("%02d", $data["TB_REMARKCD"]);
                if ($data["TB_OTHERTESTCD"]!="")
                    if (strlen($data["TB_OTHERTESTCD"]) < 2) $data["TB_OTHERTESTCD"] = sprintf("%02d", $data["TB_OTHERTESTCD"]);
                if ($data["TB_NAMECD"]!="")
                    if (strlen($data["TB_NAMECD"])      < 2) $data["TB_NAMECD"]      = sprintf("%02d", $data["TB_NAMECD"]);
                if ($data["TB_ADVISECD"]!="")
                    if (strlen($data["TB_ADVISECD"])    < 2) $data["TB_ADVISECD"]    = sprintf("%02d", $data["TB_ADVISECD"]);
                if ($data["PARASITE"]!="")
                    if (strlen($data["PARASITE"])       < 2) $data["PARASITE"]       = sprintf("%02d", $data["PARASITE"]);
                if ($data["OTHERDISEASECD"]!="")
                    if (strlen($data["OTHERDISEASECD"]) < 2) $data["OTHERDISEASECD"] = sprintf("%02d", $data["OTHERDISEASECD"]);
                if ($data["OTHER_ADVISECD"]!="")
                    if (strlen($data["OTHER_ADVISECD"]) < 2) $data["OTHER_ADVISECD"] = sprintf("%02d", $data["OTHER_ADVISECD"]);
                if ($data["DOC_CD"]!="")
                    if (strlen($data["DOC_CD"]) < 2) $data["DOC_CD"] = sprintf("%02d", $data["DOC_CD"]);
                if ($data["TREATCD"]!="")
                    if (strlen($data["TREATCD"])        < 2) $data["TREATCD"]        = sprintf("%02d", $data["TREATCD"]);
            //歯・口腔-----
            } else {
                if ($data["JAWS_JOINTCD"]!="")
                    if (strlen($data["JAWS_JOINTCD"]) < 2) $data["JAWS_JOINTCD"] = sprintf("%02d", $data["JAWS_JOINTCD"]);
                if ($data["JAWS_JOINTCD3"]!="") //熊本仕様
                    if (strlen($data["JAWS_JOINTCD3"]) < 2) $data["JAWS_JOINTCD3"] = sprintf("%02d", $data["JAWS_JOINTCD3"]);
                if ($data["JAWS_JOINTCD2"]!="")
                    if (strlen($data["JAWS_JOINTCD2"]) < 2) $data["JAWS_JOINTCD2"] = sprintf("%02d", $data["JAWS_JOINTCD2"]);
                if ($data["PLAQUECD"]!="")
                    if (strlen($data["PLAQUECD"]) < 2) $data["PLAQUECD"] = sprintf("%02d", $data["PLAQUECD"]);
                if ($data["GUMCD"]!="")
                    if (strlen($data["GUMCD"]) < 2) $data["GUMCD"] = sprintf("%02d", $data["GUMCD"]);
                if ($data["CALCULUS"]!="")
                    if (strlen($data["CALCULUS"]) < 2) $data["CALCULUS"] = sprintf("%02d", $data["CALCULUS"]);
                //乳歯 上
                if ($data["UP_R_BABY5"]!="")
                    if (strlen($data["UP_R_BABY5"]) < 2) $data["UP_R_BABY5"] = sprintf("%02d", $data["UP_R_BABY5"]);
                if ($data["UP_R_BABY4"]!="")
                    if (strlen($data["UP_R_BABY4"]) < 2) $data["UP_R_BABY4"] = sprintf("%02d", $data["UP_R_BABY4"]);
                if ($data["UP_R_BABY3"]!="")
                    if (strlen($data["UP_R_BABY3"]) < 2) $data["UP_R_BABY3"] = sprintf("%02d", $data["UP_R_BABY3"]);
                if ($data["UP_R_BABY2"]!="")
                    if (strlen($data["UP_R_BABY2"]) < 2) $data["UP_R_BABY2"] = sprintf("%02d", $data["UP_R_BABY2"]);
                if ($data["UP_R_BABY1"]!="")
                    if (strlen($data["UP_R_BABY1"]) < 2) $data["UP_R_BABY1"] = sprintf("%02d", $data["UP_R_BABY1"]);
                if ($data["UP_L_BABY1"]!="")
                    if (strlen($data["UP_L_BABY1"]) < 2) $data["UP_L_BABY1"] = sprintf("%02d", $data["UP_L_BABY1"]);
                if ($data["UP_L_BABY2"]!="")
                    if (strlen($data["UP_L_BABY2"]) < 2) $data["UP_L_BABY2"] = sprintf("%02d", $data["UP_L_BABY2"]);
                if ($data["UP_L_BABY3"]!="")
                    if (strlen($data["UP_L_BABY3"]) < 2) $data["UP_L_BABY3"] = sprintf("%02d", $data["UP_L_BABY3"]);
                if ($data["UP_L_BABY4"]!="")
                    if (strlen($data["UP_L_BABY4"]) < 2) $data["UP_L_BABY4"] = sprintf("%02d", $data["UP_L_BABY4"]);
                if ($data["UP_L_BABY5"]!="")
                    if (strlen($data["UP_L_BABY5"]) < 2) $data["UP_L_BABY5"] = sprintf("%02d", $data["UP_L_BABY5"]);
                //乳歯 下
                if ($data["LW_R_BABY5"]!="")
                    if (strlen($data["LW_R_BABY5"]) < 2) $data["LW_R_BABY5"] = sprintf("%02d", $data["LW_R_BABY5"]);
                if ($data["LW_R_BABY4"]!="")
                    if (strlen($data["LW_R_BABY4"]) < 2) $data["LW_R_BABY4"] = sprintf("%02d", $data["LW_R_BABY4"]);
                if ($data["LW_R_BABY3"]!="")
                    if (strlen($data["LW_R_BABY3"]) < 2) $data["LW_R_BABY3"] = sprintf("%02d", $data["LW_R_BABY3"]);
                if ($data["LW_R_BABY2"]!="")
                    if (strlen($data["LW_R_BABY2"]) < 2) $data["LW_R_BABY2"] = sprintf("%02d", $data["LW_R_BABY2"]);
                if ($data["LW_R_BABY1"]!="")
                    if (strlen($data["LW_R_BABY1"]) < 2) $data["LW_R_BABY1"] = sprintf("%02d", $data["LW_R_BABY1"]);
                if ($data["LW_L_BABY1"]!="")
                    if (strlen($data["LW_L_BABY1"]) < 2) $data["LW_L_BABY1"] = sprintf("%02d", $data["LW_L_BABY1"]);
                if ($data["LW_L_BABY2"]!="")
                    if (strlen($data["LW_L_BABY2"]) < 2) $data["LW_L_BABY2"] = sprintf("%02d", $data["LW_L_BABY2"]);
                if ($data["LW_L_BABY3"]!="")
                    if (strlen($data["LW_L_BABY3"]) < 2) $data["LW_L_BABY3"] = sprintf("%02d", $data["LW_L_BABY3"]);
                if ($data["LW_L_BABY4"]!="")
                    if (strlen($data["LW_L_BABY4"]) < 2) $data["LW_L_BABY4"] = sprintf("%02d", $data["LW_L_BABY4"]);
                if ($data["LW_L_BABY5"]!="")
                    if (strlen($data["LW_L_BABY5"]) < 2) $data["LW_L_BABY5"] = sprintf("%02d", $data["LW_L_BABY5"]);
                //永久歯 上
                if ($data["UP_R_ADULT8"]!="")
                    if (strlen($data["UP_R_ADULT8"]) < 2) $data["UP_R_ADULT8"] = sprintf("%02d", $data["UP_R_ADULT8"]);
                if ($data["UP_R_ADULT7"]!="")
                    if (strlen($data["UP_R_ADULT7"]) < 2) $data["UP_R_ADULT7"] = sprintf("%02d", $data["UP_R_ADULT7"]);
                if ($data["UP_R_ADULT6"]!="")
                    if (strlen($data["UP_R_ADULT6"]) < 2) $data["UP_R_ADULT6"] = sprintf("%02d", $data["UP_R_ADULT6"]);
                if ($data["UP_R_ADULT5"]!="")
                    if (strlen($data["UP_R_ADULT5"]) < 2) $data["UP_R_ADULT5"] = sprintf("%02d", $data["UP_R_ADULT5"]);
                if ($data["UP_R_ADULT4"]!="")
                    if (strlen($data["UP_R_ADULT4"]) < 2) $data["UP_R_ADULT4"] = sprintf("%02d", $data["UP_R_ADULT4"]);
                if ($data["UP_R_ADULT3"]!="")
                    if (strlen($data["UP_R_ADULT3"]) < 2) $data["UP_R_ADULT3"] = sprintf("%02d", $data["UP_R_ADULT3"]);
                if ($data["UP_R_ADULT2"]!="")
                    if (strlen($data["UP_R_ADULT2"]) < 2) $data["UP_R_ADULT2"] = sprintf("%02d", $data["UP_R_ADULT2"]);
                if ($data["UP_R_ADULT1"]!="")
                    if (strlen($data["UP_R_ADULT1"]) < 2) $data["UP_R_ADULT1"] = sprintf("%02d", $data["UP_R_ADULT1"]);
                if ($data["UP_L_ADULT1"]!="")
                    if (strlen($data["UP_L_ADULT1"]) < 2) $data["UP_L_ADULT1"] = sprintf("%02d", $data["UP_L_ADULT1"]);
                if ($data["UP_L_ADULT2"]!="")
                    if (strlen($data["UP_L_ADULT2"]) < 2) $data["UP_L_ADULT2"] = sprintf("%02d", $data["UP_L_ADULT2"]);
                if ($data["UP_L_ADULT3"]!="")
                    if (strlen($data["UP_L_ADULT3"]) < 2) $data["UP_L_ADULT3"] = sprintf("%02d", $data["UP_L_ADULT3"]);
                if ($data["UP_L_ADULT4"]!="")
                    if (strlen($data["UP_L_ADULT4"]) < 2) $data["UP_L_ADULT4"] = sprintf("%02d", $data["UP_L_ADULT4"]);
                if ($data["UP_L_ADULT5"]!="")
                    if (strlen($data["UP_L_ADULT5"]) < 2) $data["UP_L_ADULT5"] = sprintf("%02d", $data["UP_L_ADULT5"]);
                if ($data["UP_L_ADULT6"]!="")
                    if (strlen($data["UP_L_ADULT6"]) < 2) $data["UP_L_ADULT6"] = sprintf("%02d", $data["UP_L_ADULT6"]);
                if ($data["UP_L_ADULT7"]!="")
                    if (strlen($data["UP_L_ADULT7"]) < 2) $data["UP_L_ADULT7"] = sprintf("%02d", $data["UP_L_ADULT7"]);
                if ($data["UP_L_ADULT8"]!="")
                    if (strlen($data["UP_L_ADULT8"]) < 2) $data["UP_L_ADULT8"] = sprintf("%02d", $data["UP_L_ADULT8"]);
                //永久歯 下
                if ($data["LW_R_ADULT8"]!="")
                    if (strlen($data["LW_R_ADULT8"]) < 2) $data["LW_R_ADULT8"] = sprintf("%02d", $data["LW_R_ADULT8"]);
                if ($data["LW_R_ADULT7"]!="")
                    if (strlen($data["LW_R_ADULT7"]) < 2) $data["LW_R_ADULT7"] = sprintf("%02d", $data["LW_R_ADULT7"]);
                if ($data["LW_R_ADULT6"]!="")
                    if (strlen($data["LW_R_ADULT6"]) < 2) $data["LW_R_ADULT6"] = sprintf("%02d", $data["LW_R_ADULT6"]);
                if ($data["LW_R_ADULT5"]!="")
                    if (strlen($data["LW_R_ADULT5"]) < 2) $data["LW_R_ADULT5"] = sprintf("%02d", $data["LW_R_ADULT5"]);
                if ($data["LW_R_ADULT4"]!="")
                    if (strlen($data["LW_R_ADULT4"]) < 2) $data["LW_R_ADULT4"] = sprintf("%02d", $data["LW_R_ADULT4"]);
                if ($data["LW_R_ADULT3"]!="")
                    if (strlen($data["LW_R_ADULT3"]) < 2) $data["LW_R_ADULT3"] = sprintf("%02d", $data["LW_R_ADULT3"]);
                if ($data["LW_R_ADULT2"]!="")
                    if (strlen($data["LW_R_ADULT2"]) < 2) $data["LW_R_ADULT2"] = sprintf("%02d", $data["LW_R_ADULT2"]);
                if ($data["LW_R_ADULT1"]!="")
                    if (strlen($data["LW_R_ADULT1"]) < 2) $data["LW_R_ADULT1"] = sprintf("%02d", $data["LW_R_ADULT1"]);
                if ($data["LW_L_ADULT1"]!="")
                    if (strlen($data["LW_L_ADULT1"]) < 2) $data["LW_L_ADULT1"] = sprintf("%02d", $data["LW_L_ADULT1"]);
                if ($data["LW_L_ADULT2"]!="")
                    if (strlen($data["LW_L_ADULT2"]) < 2) $data["LW_L_ADULT2"] = sprintf("%02d", $data["LW_L_ADULT2"]);
                if ($data["LW_L_ADULT3"]!="")
                    if (strlen($data["LW_L_ADULT3"]) < 2) $data["LW_L_ADULT3"] = sprintf("%02d", $data["LW_L_ADULT3"]);
                if ($data["LW_L_ADULT4"]!="")
                    if (strlen($data["LW_L_ADULT4"]) < 2) $data["LW_L_ADULT4"] = sprintf("%02d", $data["LW_L_ADULT4"]);
                if ($data["LW_L_ADULT5"]!="")
                    if (strlen($data["LW_L_ADULT5"]) < 2) $data["LW_L_ADULT5"] = sprintf("%02d", $data["LW_L_ADULT5"]);
                if ($data["LW_L_ADULT6"]!="")
                    if (strlen($data["LW_L_ADULT6"]) < 2) $data["LW_L_ADULT6"] = sprintf("%02d", $data["LW_L_ADULT6"]);
                if ($data["LW_L_ADULT7"]!="")
                    if (strlen($data["LW_L_ADULT7"]) < 2) $data["LW_L_ADULT7"] = sprintf("%02d", $data["LW_L_ADULT7"]);
                if ($data["LW_L_ADULT8"]!="")
                    if (strlen($data["LW_L_ADULT8"]) < 2) $data["LW_L_ADULT8"] = sprintf("%02d", $data["LW_L_ADULT8"]);
                //その他疾病及異常コード
                if ($data["OTHERDISEASECD"]!="")
                    if (strlen($data["OTHERDISEASECD"]) < 2) $data["OTHERDISEASECD"] = sprintf("%02d", $data["OTHERDISEASECD"]);
                //学校歯科医所見コード
                if ($data["DENTISTREMARKCD"]!="")
                    if (strlen($data["DENTISTREMARKCD"]) < 2) $data["DENTISTREMARKCD"] = sprintf("%02d", $data["DENTISTREMARKCD"]);                
                //学校歯科医事後処置コード
                if ($data["DENTISTTREATCD"]!="")
                    if (strlen($data["DENTISTTREATCD"]) < 2) $data["DENTISTTREATCD"] = sprintf("%02d", $data["DENTISTTREATCD"]);
            }//csvcd
        //削除
        } else {
            if ($data["SCHREGNO"]!="")
                if (strlen($data["SCHREGNO"]) < 8) $data["SCHREGNO"] = sprintf("%08d", $data["SCHREGNO"]);
        }

        return $data;
    }

    /**
     * マスタの存在チェック 項目ﾁｪｯｸ後に行う。
     */
    function sonzai_check(&$db,$data) {
        $sonzai_error = "";        //存在エラー用
        //2.在籍データ
        if( $data["YEAR"]!="" && $data["SCHREGNO"]!="") {
            $sonzai_flg = false;
            $query = knjf140aQuery::getMasterCheck($data,"2");
            if ($db->getOne($query) > 0) $sonzai_flg = true;
            if(!$sonzai_flg) $sonzai_error .= "在籍データなし。";
        }

        //1.名称マスタ
        $this->name_mst_err = array();
        //一般-----
        if ($this->field["CSVCD"] == "1") {
            $this->sonzai_check2($db,$data,"F010",$data["R_EAR"]);
            $this->sonzai_check2($db,$data,"F010",$data["L_EAR"]);
            $this->sonzai_check2($db,$data,"F020",$data["ALBUMINURIA1CD"]);
            $this->sonzai_check2($db,$data,"F020",$data["URICSUGAR1CD"]);
            $this->sonzai_check2($db,$data,"F020",$data["URICBLEED1CD"]);
            $this->sonzai_check2($db,$data,"F020",$data["ALBUMINURIA2CD"]);
            $this->sonzai_check2($db,$data,"F020",$data["URICSUGAR2CD"]);
            $this->sonzai_check2($db,$data,"F020",$data["URICBLEED2CD"]);
            $this->sonzai_check2($db,$data,"F020",$data["TB_RESULT"]);
            $this->sonzai_check2($db,$data,"F021",$data["URI_ADVISECD"]);
            $this->sonzai_check2($db,$data,"F030",$data["NUTRITIONCD"]);
            $this->sonzai_check2($db,$data,"F040",$data["SPINERIBCD"]);
            $this->sonzai_check2($db,$data,"F050",$data["EYEDISEASECD"]);
            $this->sonzai_check2($db,$data,"F060",$data["NOSEDISEASECD"]);
            $this->sonzai_check2($db,$data,"F070",$data["SKINDISEASECD"]);
            $this->sonzai_check2($db,$data,"F080",$data["HEART_MEDEXAM"]);
            $this->sonzai_check2($db,$data,"F091",$data["MANAGEMENT_DIV"]);
            $this->sonzai_check2($db,$data,"F090",$data["HEARTDISEASECD"]);
            $this->sonzai_check2($db,$data,"F100",$data["TB_REMARKCD"]);
            $this->sonzai_check2($db,$data,"F110",$data["TB_OTHERTESTCD"]);
            $this->sonzai_check2($db,$data,"F120",$data["TB_NAMECD"]);
            $this->sonzai_check2($db,$data,"F130",$data["TB_ADVISECD"]);
            $this->sonzai_check2($db,$data,"F140",$data["OTHERDISEASECD"]);
            $this->sonzai_check2($db,$data,"F145",$data["OTHER_ADVISECD"]);
            $this->sonzai_check2($db,$data,"F144",$data["DOC_CD"]);
            $this->sonzai_check2($db,$data,"F150",$data["TREATCD"]);
            $this->sonzai_check2($db,$data,"F141",$data["GUIDE_DIV"]);
            $this->sonzai_check2($db,$data,"F142",$data["JOINING_SPORTS_CLUB"]);
            $this->sonzai_check2($db,$data,"F143",$data["MEDICAL_HISTORY1"]);
            $this->sonzai_check2($db,$data,"F143",$data["MEDICAL_HISTORY2"]);
            $this->sonzai_check2($db,$data,"F143",$data["MEDICAL_HISTORY3"]);
        //歯・口腔-----
        } else {
            $this->sonzai_check2($db,$data,"F510",$data["JAWS_JOINTCD"]);
            $this->sonzai_check2($db,$data,"F512",$data["JAWS_JOINTCD3"]);//熊本仕様
            $this->sonzai_check2($db,$data,"F511",$data["JAWS_JOINTCD2"]);
            $this->sonzai_check2($db,$data,"F520",$data["PLAQUECD"]);
            $this->sonzai_check2($db,$data,"F510",$data["GUMCD"]);
            $this->sonzai_check2($db,$data,"F521",$data["CALCULUS"]);

            $this->sonzai_check2($db,$data,"F550",$data["UP_R_BABY5"]); //乳歯
            $this->sonzai_check2($db,$data,"F550",$data["UP_R_BABY4"]);
            $this->sonzai_check2($db,$data,"F550",$data["UP_R_BABY3"]);
            $this->sonzai_check2($db,$data,"F550",$data["UP_R_BABY2"]);
            $this->sonzai_check2($db,$data,"F550",$data["UP_R_BABY1"]);
            $this->sonzai_check2($db,$data,"F550",$data["UP_L_BABY1"]);
            $this->sonzai_check2($db,$data,"F550",$data["UP_L_BABY2"]);
            $this->sonzai_check2($db,$data,"F550",$data["UP_L_BABY3"]);
            $this->sonzai_check2($db,$data,"F550",$data["UP_L_BABY4"]);
            $this->sonzai_check2($db,$data,"F550",$data["UP_L_BABY5"]);
            $this->sonzai_check2($db,$data,"F550",$data["LW_R_BABY5"]);
            $this->sonzai_check2($db,$data,"F550",$data["LW_R_BABY4"]);
            $this->sonzai_check2($db,$data,"F550",$data["LW_R_BABY3"]);
            $this->sonzai_check2($db,$data,"F550",$data["LW_R_BABY2"]);
            $this->sonzai_check2($db,$data,"F550",$data["LW_R_BABY1"]);
            $this->sonzai_check2($db,$data,"F550",$data["LW_L_BABY1"]);
            $this->sonzai_check2($db,$data,"F550",$data["LW_L_BABY2"]);
            $this->sonzai_check2($db,$data,"F550",$data["LW_L_BABY3"]);
            $this->sonzai_check2($db,$data,"F550",$data["LW_L_BABY4"]);
            $this->sonzai_check2($db,$data,"F550",$data["LW_L_BABY5"]);

            $this->sonzai_check2($db,$data,"F550",$data["UP_R_ADULT8"]); //永久歯
            $this->sonzai_check2($db,$data,"F550",$data["UP_R_ADULT7"]);
            $this->sonzai_check2($db,$data,"F550",$data["UP_R_ADULT6"]);
            $this->sonzai_check2($db,$data,"F550",$data["UP_R_ADULT5"]);
            $this->sonzai_check2($db,$data,"F550",$data["UP_R_ADULT4"]);
            $this->sonzai_check2($db,$data,"F550",$data["UP_R_ADULT3"]);
            $this->sonzai_check2($db,$data,"F550",$data["UP_R_ADULT2"]);
            $this->sonzai_check2($db,$data,"F550",$data["UP_R_ADULT1"]);
            $this->sonzai_check2($db,$data,"F550",$data["UP_L_ADULT1"]);
            $this->sonzai_check2($db,$data,"F550",$data["UP_L_ADULT2"]);
            $this->sonzai_check2($db,$data,"F550",$data["UP_L_ADULT3"]);
            $this->sonzai_check2($db,$data,"F550",$data["UP_L_ADULT4"]);
            $this->sonzai_check2($db,$data,"F550",$data["UP_L_ADULT5"]);
            $this->sonzai_check2($db,$data,"F550",$data["UP_L_ADULT6"]);
            $this->sonzai_check2($db,$data,"F550",$data["UP_L_ADULT7"]);
            $this->sonzai_check2($db,$data,"F550",$data["UP_L_ADULT8"]);
            $this->sonzai_check2($db,$data,"F550",$data["LW_R_ADULT8"]);
            $this->sonzai_check2($db,$data,"F550",$data["LW_R_ADULT7"]);
            $this->sonzai_check2($db,$data,"F550",$data["LW_R_ADULT6"]);
            $this->sonzai_check2($db,$data,"F550",$data["LW_R_ADULT5"]);
            $this->sonzai_check2($db,$data,"F550",$data["LW_R_ADULT4"]);
            $this->sonzai_check2($db,$data,"F550",$data["LW_R_ADULT3"]);
            $this->sonzai_check2($db,$data,"F550",$data["LW_R_ADULT2"]);
            $this->sonzai_check2($db,$data,"F550",$data["LW_R_ADULT1"]);
            $this->sonzai_check2($db,$data,"F550",$data["LW_L_ADULT1"]);
            $this->sonzai_check2($db,$data,"F550",$data["LW_L_ADULT2"]);
            $this->sonzai_check2($db,$data,"F550",$data["LW_L_ADULT3"]);
            $this->sonzai_check2($db,$data,"F550",$data["LW_L_ADULT4"]);
            $this->sonzai_check2($db,$data,"F550",$data["LW_L_ADULT5"]);
            $this->sonzai_check2($db,$data,"F550",$data["LW_L_ADULT6"]);
            $this->sonzai_check2($db,$data,"F550",$data["LW_L_ADULT7"]);
            $this->sonzai_check2($db,$data,"F550",$data["LW_L_ADULT8"]);

            $this->sonzai_check2($db,$data,"F530",$data["OTHERDISEASECD"]);
            $this->sonzai_check2($db,$data,"F540",$data["DENTISTREMARKCD"]);
            $this->sonzai_check2($db,$data,"F541",$data["DENTISTTREATCD"]);
        }//csvcd
        if (get_count($this->name_mst_err) > 0) $sonzai_error .= "名称マスタなし(".implode("：", $this->name_mst_err).")。";

        return $sonzai_error;    //存在エラー用
    }

    /**
     * 名称マスタの存在チェック 項目ﾁｪｯｸ後に行う。
     */
    function sonzai_check2(&$db,$data,$namecd1,$namecd2) {
        if ($namecd2!="") {
            $sonzai_flg = false;
            $query = knjf140aQuery::getMasterCheck($data,"1",$namecd1,$namecd2);
            if ($db->getOne($query) > 0) $sonzai_flg = true;
            if(!$sonzai_flg) $this->name_mst_err[$namecd1] = $namecd1;
        }
    }

    //視力の文字チェック
    function siryokumoji_check(&$mark, $suuji, $db) {
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
                    return "未対応の文字(視力)";
                    break;
            }
        } elseif ($suuji) {
            $query = knjf140aQuery::getSiryokuMark($suuji); //視力も文字取得
            $mark = $db->getOne($query);
        }
    }
}
?>
