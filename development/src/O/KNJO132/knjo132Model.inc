<?php
//require_once('dBug.php');
require_once('for_php7.php');

class knjo132Model extends Model {



    var $grade;
    var $cmd;
    var $examyear;
    var $recordTableDiv;
    var $schoolCd;
    var $getPrgId;  //コールされたか
    var $sendAuth;  //親画面の権限
    var $auth;      //権限


    function knjo132Model()
    {
        $this->examyear = CTRL_YEAR + 1;
    }

    function init()
    {
        $this->valErrMess = array();
        $this->valErrFlg = "";
        $this->new = array();

        $this->cmd   = VARS::request("cmd");
        $this->SCHREGNO = VARS::post("SCHREGNO");
        $this->SEND = VARS::post("SEND") != "" ? VARS::post("SEND") : "1";
        
        $this->field = array("K_ID"         => VARS::post("K_ID"),
                             
                             "K_NAME"       => VARS::post("K_NAME"),
                             
                             "K_BKK"        => VARS::post("K_BKK"),
                             
                             "K_ADDRCD"     => VARS::post("K_ADDRCD"),
                             
                             "K_ZIPCD"      => VARS::post("K_ZIPCD"),
                             
                             "K_ADDRESS"    => VARS::post("K_ADDRESS"),
                             
                             "K_KATAGAKI"   => VARS::post("K_KATAGAKI"),
                             
                             "K_TELNO"      => VARS::post("K_TELNO"),
                             
                             "K_SYOKU"      => VARS::post("K_SYOKU"),
                             
                             "G_ID"         => VARS::post("G_ID"),
                             
                             "G_NAME"       => VARS::post("G_NAME"),
                             
                             "G_ADDRCD"     => VARS::post("G_ADDRCD"),
                             
                             "G_ZIPCD"      => VARS::post("G_ZIPCD"),
                             
                             "G_ADDRESS"    => VARS::post("G_ADDRESS"),
                             
                             "G_KATAGAKI"   => VARS::post("G_KATAGAKI"),
                             
                             "G_TELNO"      => VARS::post("G_TELNO"),
                             
                             "G_SYOKU"      => VARS::post("G_SYOKU"),
                             
                             "G_BIKOU"      => VARS::post("G_BIKOU"),
                             
                             "G_BUNID"         => VARS::post("G_BUNID"),
                             
                             "G_BUNNAME"       => VARS::post("G_BUNNAME"),
                             
                             "G_BUNADDRCD"     => VARS::post("G_BUNADDRCD"),
                             
                             "G_BUNZIPCD"      => VARS::post("G_BUNZIPCD"),
                             
                             "G_BUNADDRESS"    => VARS::post("G_BUNADDRESS"),
                             
                             "G_BUNKATAGAKI"   => VARS::post("G_BUNKATAGAKI"),
                             
                            );
    }

    public function schregChk($schregno)
    {
        $row = knjo132Query::getSchregregdData($this);
        if (!isset($row)) {
            $this->setWarning("", "該当する学籍番号はありません。");
            return false;
        }
        return true;
    }

    function updateModel()
    {
        $flg = false;
        
        //プロパティファイル読み込み
        $this->getProperties();

        //DB接続
        $db = Query::dbCheckOut();

        //エラーチェック
        $query = knjo132Query::getCheck($this->SCHREGNO);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        if($row["CNT"] == ""){
            $this->setMessage("対象のデータはありません。");
            return false;
        }else{
            if($row["SCHOOL_KIND"] != "H"){
                $this->schoolKind = $row["SCHOOL_KIND"];
            }else{  //SCHOOL_KINDが高校だったらエラーにする
                $this->setMessage("年度対象外です。");
                return false;
            }
        }
        
        $data = array();
        
        //学校が小学校か中学校か判断する用フラグ
        //$this->schoolKind = "P";    //小学校
        //$this->schoolKind = "J";  //中学校
        
        //データ作成
        $data["利用業務ユニット"] = "AK02";
        
        //送信先
        if($this->SEND == "1"){
            if($this->field["K_ID"] != ""){
                $data["送信先情報"]["教育委員会"]["教育委員会ID"] = $this->field["K_ID"];
            }
            $data["送信先情報"]["教育委員会"]["外字教育委員会名"] = "";
            $data["送信先情報"]["教育委員会"]["内字教育委員会名"] = $this->field["K_NAME"];
            if($this->field["K_BBK"] != ""){
                $data["送信先情報"]["教育委員会"]["部課係"] = $this->field["K_BKK"];
            }
            if($this->field["K_ADDRCD"] != "" || $this->field["K_ADDRESS"] != "" || $this->field["K_KATAGAKI"] != "" || $this->field["K_ZIPCD"] != ""){
                $data["送信先情報"]["教育委員会"]["所在地"]["lgxml:住所コード"] = $this->field["K_ADDRCD"];
                $data["送信先情報"]["教育委員会"]["所在地"]["lgxml:外字住所"] = "";
                $data["送信先情報"]["教育委員会"]["所在地"]["lgxml:内字住所"] = $this->field["K_ADDRESS"];
                $data["送信先情報"]["教育委員会"]["所在地"]["lgxml:外字方書"] = "";
                $data["送信先情報"]["教育委員会"]["所在地"]["lgxml:内字方書"] = $this->field["K_KATAGAKI"];
                $data["送信先情報"]["教育委員会"]["所在地"]["lgxml:郵便番号"] = $this->field["K_ZIPCD"];
            }
            if($this->field["K_TELNO"] != ""){
                $data["送信先情報"]["教育委員会"]["電話番号"] = $this->field["K_TELNO"];
            }
            if($this->field["K_SYOKU"] != ""){
                $data["送信先情報"]["教育委員会"]["職"] = $this->field["K_SYOKU"];
            }
        }else{
            //学校
            if($this->field["G_ID"] != ""){
                $data["送信先情報"]["学校"]["学校ID"] = $this->field["G_ID"];
            }
            $data["送信先情報"]["学校"]["外字学校名"] = "";
            $data["送信先情報"]["学校"]["内字学校名"] = $this->field["G_NAME"];
            $data["送信先情報"]["学校"]["所在地"]["lgxml:住所コード"] = $this->field["G_ADDRCD"];
            $data["送信先情報"]["学校"]["所在地"]["lgxml:外字住所"] = "";
            $data["送信先情報"]["学校"]["所在地"]["lgxml:内字住所"] = $this->field["G_ADDRESS"];
            $data["送信先情報"]["学校"]["所在地"]["lgxml:外字方書"] = "";
            $data["送信先情報"]["学校"]["所在地"]["lgxml:内字方書"] = $this->field["G_KATAGAKI"];
            $data["送信先情報"]["学校"]["所在地"]["lgxml:郵便番号"] = $this->field["G_ZIPCD"];
            if($this->field["G_BUNID"] != ""){
                $data["送信先情報"]["学校"]["分校ID"] = $this->field["G_BUNID"];
            }
            if($this->field["G_BUNNAME"] != ""){
                $data["送信先情報"]["学校"]["外字分校名"] = "";
                $data["送信先情報"]["学校"]["内字分校名"] = $this->field["G_BUNNAME"];
            }
            if($this->field["G_BUNADDRCD"] != "" || $this->field["G_BUNADDRESS"] != "" || $this->field["G_BUNKATAGAKI"] != "" || $this->field["G_BUNZIPCD"] != ""){
                $data["送信先情報"]["学校"]["分校所在地"]["lgxml:住所コード"] = $this->field["G_BUNADDRCD"];
                $data["送信先情報"]["学校"]["分校所在地"]["lgxml:外字住所"] = "";
                $data["送信先情報"]["学校"]["分校所在地"]["lgxml:内字住所"] = $this->field["G_BUNADDRESS"];
                $data["送信先情報"]["学校"]["分校所在地"]["lgxml:外字方書"] = "";
                $data["送信先情報"]["学校"]["分校所在地"]["lgxml:内字方書"] = $this->field["G_BUNKATAGAKI"];
                $data["送信先情報"]["学校"]["分校所在地"]["lgxml:郵便番号"] = $this->field["G_BUNZIPCD"];
            }
            if($this->field["G_TELNO"] != ""){
                $data["送信先情報"]["学校"]["電話番号"] = $this->field["G_TELNO"];
            }
            if($this->field["G_SYOKU"] != ""){
                $data["送信先情報"]["学校"]["職"] = $this->field["G_SYOKU"];
            }
            if($this->field["G_BIKOU"] != ""){
                $data["送信先情報"]["学校"]["備考"] = $this->field["G_BIKOU"];
            }
        }
        
        //送信元
        $query = knjo132Query::getMotoSchool($this->schoolKind);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        
        $data["送信元情報"]["学校"]["外字学校名"] = "";
        $data["送信元情報"]["学校"]["内字学校名"] = $row["SCHOOLNAME1"];
        $data["送信元情報"]["学校"]["所在地"]["lgxml:住所コード"] = "";
        $data["送信元情報"]["学校"]["所在地"]["lgxml:外字住所"] = "";
        $data["送信元情報"]["学校"]["所在地"]["lgxml:内字住所"] = $row["SCHOOLADDR1"];
        $data["送信元情報"]["学校"]["所在地"]["lgxml:外字方書"] = "";
        $data["送信元情報"]["学校"]["所在地"]["lgxml:内字方書"] = $row["SCHOOLADDR2"];
        $data["送信元情報"]["学校"]["所在地"]["lgxml:郵便番号"] = $row["SCHOOLZIPCD"];
        if($row["SCHOOLTELNO"] != ""){
            $data["送信元情報"]["学校"]["電話番号"] = $row["SCHOOLTELNO"];
        }
        
        //出力ファイル名用に保持
        $this->schoolName = $row["SCHOOLNAME1"];
        
        //児童生徒健康診断票情報
        $data["児童生徒健康診断票情報"]["健康診断種別"] = "H28";    //様式どこから判断するのか…
        
        //児童生徒健康診断票情報＞児童生徒＞基本情報
        $query = knjo132Query::getStudent($this->SCHREGNO);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        
        $data["児童生徒健康診断票情報"]["児童生徒"]["基本情報"]["氏名"]["lgxml:外字氏名"] = "";
        $data["児童生徒健康診断票情報"]["児童生徒"]["基本情報"]["氏名"]["lgxml:内字氏名"] = $row["NAME"];
        $data["児童生徒健康診断票情報"]["児童生徒"]["基本情報"]["氏名"]["lgxml:フリガナ"] = $row["NAME_KANA"];
        
        if($row["NAME_SHOW"] != ""){
            $data["児童生徒健康診断票情報"]["児童生徒"]["基本情報"]["通称名"]["lgxml:外字氏名"] = "";
            $data["児童生徒健康診断票情報"]["児童生徒"]["基本情報"]["通称名"]["lgxml:内字氏名"] = $row["NAME_SHOW"];
            $data["児童生徒健康診断票情報"]["児童生徒"]["基本情報"]["通称名"]["lgxml:フリガナ"] = $row["NAME_KANA"];
        }
        
        $data["児童生徒健康診断票情報"]["児童生徒"]["基本情報"]["性別"] = $row["SEX"];
        
        $data["児童生徒健康診断票情報"]["児童生徒"]["基本情報"]["生年月日"] = $row["BIRTHDAY"];
        //健康診断一般とかに使う
        $this->birthday = str_replace("-", "", $row["BIRTHDAY"]);
        
        //児童生徒＞現住所
        $data["児童生徒健康診断票情報"]["児童生徒"]["基本情報"]["現住所"]["住所"]["lgxml:住所コード"] = "";
        $data["児童生徒健康診断票情報"]["児童生徒"]["基本情報"]["現住所"]["住所"]["lgxml:外字住所"] = "";
        $data["児童生徒健康診断票情報"]["児童生徒"]["基本情報"]["現住所"]["住所"]["lgxml:内字住所"] = $row["ADDR1"];
        $data["児童生徒健康診断票情報"]["児童生徒"]["基本情報"]["現住所"]["住所"]["lgxml:外字方書"] = "";
        if($row["ADDR_FLG"] == "1"){
            $data["児童生徒健康診断票情報"]["児童生徒"]["基本情報"]["現住所"]["住所"]["lgxml:内字方書"] = $row["ADDR2"];
        }else{
            $data["児童生徒健康診断票情報"]["児童生徒"]["基本情報"]["現住所"]["住所"]["lgxml:内字方書"] = "";
        }
        $data["児童生徒健康診断票情報"]["児童生徒"]["基本情報"]["現住所"]["住所"]["lgxml:郵便番号"] = $row["ZIPCD"];
        
        if($row["NATION"] != ""){
            $data["児童生徒健康診断票情報"]["児童生徒"]["基本情報"]["国籍"] = $row["NATION"];
        }
        
        //児童生徒健康診断票情報＞児童生徒＞在学学年
        $zaiCnt = 0;
        
        $query = knjo132Query::getZaigaku($this->SCHREGNO, "1");
        $cnt = $db->getOne($query);
        
        if($cnt > 0){
            $query = knjo132Query::getZaigaku($this->SCHREGNO);
            $result = $db->query($query);
            
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                if($zaiCnt > 8){
                    break;  //最大9回まで
                }
                $data["児童生徒健康診断票情報"]["児童生徒"]["在学学年"][$zaiCnt]["学年"] = $row["GRADE"];
                if($row["HR_CLASS_NAME1"] != ""){
                    $data["児童生徒健康診断票情報"]["児童生徒"]["在学学年"][$zaiCnt]["学級"] = $row["HR_CLASS_NAME1"];
                }else{
                    $data["児童生徒健康診断票情報"]["児童生徒"]["在学学年"][$zaiCnt]["学級"] = $row["HR_CLASS"];
                }
                $data["児童生徒健康診断票情報"]["児童生徒"]["在学学年"][$zaiCnt]["番号"] = $row["ATTENDNO"];
                
                $zaiCnt++;
            }
        }
        
        //小学校か中学校    送信元情報と同じでよい
        $query = knjo132Query::getMotoSchool($this->schoolKind);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        if($this->schoolKind == "P"){
            //小学校
            $data["児童生徒健康診断票情報"]["児童生徒"]["小学校"]["外字学校名"] = "";
            $data["児童生徒健康診断票情報"]["児童生徒"]["小学校"]["内字学校名"] = $row["SCHOOLNAME1"];
            $data["児童生徒健康診断票情報"]["児童生徒"]["小学校"]["所在地"]["lgxml:住所コード"] = "";
            $data["児童生徒健康診断票情報"]["児童生徒"]["小学校"]["所在地"]["lgxml:外字住所"] = "";
            $data["児童生徒健康診断票情報"]["児童生徒"]["小学校"]["所在地"]["lgxml:内字住所"] = $row["SCHOOLADDR1"];
            $data["児童生徒健康診断票情報"]["児童生徒"]["小学校"]["所在地"]["lgxml:外字方書"] = "";
            $data["児童生徒健康診断票情報"]["児童生徒"]["小学校"]["所在地"]["lgxml:内字方書"] = $row["SCHOOLADDR2"];
            $data["児童生徒健康診断票情報"]["児童生徒"]["小学校"]["所在地"]["lgxml:郵便番号"] = $row["SCHOOLZIPCD"];
            $data["児童生徒健康診断票情報"]["児童生徒"]["小学校"]["電話番号"] = $row["SCHOOLTELNO"];
        }else{
            //中学校
            $data["児童生徒健康診断票情報"]["児童生徒"]["中学校"]["外字学校名"] = "";
            $data["児童生徒健康診断票情報"]["児童生徒"]["中学校"]["内字学校名"] = $row["SCHOOLNAME1"];
            $data["児童生徒健康診断票情報"]["児童生徒"]["中学校"]["所在地"]["lgxml:住所コード"] = "";
            $data["児童生徒健康診断票情報"]["児童生徒"]["中学校"]["所在地"]["lgxml:外字住所"] = "";
            $data["児童生徒健康診断票情報"]["児童生徒"]["中学校"]["所在地"]["lgxml:内字住所"] = $row["SCHOOLADDR1"];
            $data["児童生徒健康診断票情報"]["児童生徒"]["中学校"]["所在地"]["lgxml:外字方書"] = "";
            $data["児童生徒健康診断票情報"]["児童生徒"]["中学校"]["所在地"]["lgxml:内字方書"] = $row["SCHOOLADDR2"];
            $data["児童生徒健康診断票情報"]["児童生徒"]["中学校"]["所在地"]["lgxml:郵便番号"] = $row["SCHOOLZIPCD"];
            $data["児童生徒健康診断票情報"]["児童生徒"]["中学校"]["電話番号"] = $row["SCHOOLTELNO"];
        }
        
        //健康診断一般
        $medCnt = 0;
        $query = knjo132Query::getMedical($this->SCHREGNO, "1");
        $cnt = $db->getOne($query);
        
        if($cnt > 0){
            $query = knjo132Query::getMedical($this->SCHREGNO);
            $result = $db->query($query);
            
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                if($medCnt > 8){
                    break;
                }
                
                //年度
                $data["児童生徒健康診断票情報"]["健康診断一般"][$medCnt]["年度"] = $row["YEAR"];
                
                //年齢計算
                $april = $row["YEAR"]."0401";
                $age = floor(($april - $this->birthday)/10000);
                $data["児童生徒健康診断票情報"]["健康診断一般"][$medCnt]["年齢"] = $age;
                
                //発育測定
                if($row["HEIGHT"] != "" || $row["WEIGHT"] != "" || $row["SITHEIGHT"] != ""){
                    $data["児童生徒健康診断票情報"]["健康診断一般"][$medCnt]["発育測定"]["身長"] = sprintf("%0.1f", $row["HEIGHT"]);
                    $data["児童生徒健康診断票情報"]["健康診断一般"][$medCnt]["発育測定"]["体重"] = sprintf("%0.1f", $row["WEIGHT"]);
                    if($row["SITHEIGHT"] != ""){
                        $data["児童生徒健康診断票情報"]["健康診断一般"][$medCnt]["発育測定"]["座高"] = sprintf("%0.1f", $row["SITHEIGHT"]);
                    }
                }
                
                //栄養状態
                if($row["NUTRITIONCD"] != ""){
                    if($row["NUTRITIONCD"] == "01"){
                        $data["児童生徒健康診断票情報"]["健康診断一般"][$medCnt]["栄養状態"]["異常の有無"] = "0";
                    }else{
                        $data["児童生徒健康診断票情報"]["健康診断一般"][$medCnt]["栄養状態"]["異常の有無"] = "1";
                        $data["児童生徒健康診断票情報"]["健康診断一般"][$medCnt]["栄養状態"]["外字疾病名"] = "";
                        $data["児童生徒健康診断票情報"]["健康診断一般"][$medCnt]["栄養状態"]["内字疾病名"] = $row["NUTDISEASE"];
                    }
                }else{
                    $data["児童生徒健康診断票情報"]["健康診断一般"][$medCnt]["栄養状態"]["異常の有無"] = "2";
                }
                
                //脊柱胸郭四肢  疾病及び異常を作成するものとする
                if($row["SPINERIBCD"] != ""){
                    if($row["SPINERIBCD"] == "01"){
                        $data["児童生徒健康診断票情報"]["健康診断一般"][$medCnt]["脊柱胸郭四肢"]["疾病及び異常"]["異常の有無"] = "0";
                    }else{
                        $data["児童生徒健康診断票情報"]["健康診断一般"][$medCnt]["脊柱胸郭四肢"]["疾病及び異常"]["異常の有無"] = "1";
                        $data["児童生徒健康診断票情報"]["健康診断一般"][$medCnt]["脊柱胸郭四肢"]["疾病及び異常"]["外字疾病名"] = "";
                        if($row["SPINERIBCD_REMARK"] != ""){
                            $data["児童生徒健康診断票情報"]["健康診断一般"][$medCnt]["脊柱胸郭四肢"]["疾病及び異常"]["内字疾病名"] = $row["SPINERIBCD_REMARK"];
                        }else{
                            $data["児童生徒健康診断票情報"]["健康診断一般"][$medCnt]["脊柱胸郭四肢"]["疾病及び異常"]["内字疾病名"] = $row["SPINERIBDISEASE"];
                        }
                    }
                }else{
                    $data["児童生徒健康診断票情報"]["健康診断一般"][$medCnt]["脊柱胸郭四肢"]["疾病及び異常"]["異常の有無"] = "2";
                }
                //脊柱・胸郭・四肢それぞれのタグは作成しない
                
                //視力
                $query = knjo132Query::getCode("02");
                $visResult = $db->query($query);
                $vision = array();
                while($visRow = $visResult->fetchRow(DB_FETCHMODE_ASSOC)){
                    $vision[$visRow["KENJA_CODE"]] = $visRow["DICTIONARY_CODE"];
                }
                /*$vision = array("A" => "1",
                                "B" => "2",
                                "C" => "3",
                                "D" => "4");*/
                //裸眼  コードがA～DなのでMARKだけ使う
                if($row["R_BAREVISION_MARK"] != "" || $row["L_BAREVISION_MARK"] != ""){
                    if($row["R_BAREVISION_MARK"] != ""){
                        //全角アルファベットの可能性があるので半角に変換したい
                        $rMark = mb_convert_kana($row["R_BAREVISION_MARK"], "r");
                        if(array_key_exists($rMark, $vision)){
                            $data["児童生徒健康診断票情報"]["健康診断一般"][$medCnt]["視力"]["裸眼"]["右"] = $vision[$rMark];
                        }else{
                            $data["児童生徒健康診断票情報"]["健康診断一般"][$medCnt]["視力"]["裸眼"]["右"] = "9";   //測定不能
                        }
                    }else{
                        $data["児童生徒健康診断票情報"]["健康診断一般"][$medCnt]["視力"]["裸眼"]["右"] = "5";   //未検査
                    }
                    if($row["L_BAREVISION_MARK"] != ""){
                        //全角アルファベットの可能性があるので半角に変換したい
                        $lMark = mb_convert_kana($row["L_BAREVISION_MARK"], "r");
                        if(array_key_exists($lMark, $vision)){
                            $data["児童生徒健康診断票情報"]["健康診断一般"][$medCnt]["視力"]["裸眼"]["左"] = $vision[$lMark];
                        }else{
                            $data["児童生徒健康診断票情報"]["健康診断一般"][$medCnt]["視力"]["裸眼"]["左"] = "9";   //測定不能
                        }
                    }else{
                        $data["児童生徒健康診断票情報"]["健康診断一般"][$medCnt]["視力"]["裸眼"]["左"] = "5";   //未検査
                    }
                }
                //矯正  コードがA～DなのでMARKだけ使う
                if($row["R_VISION_MARK"] != "" || $row["L_VISION_MARK"] != ""){
                    if($row["R_VISION_MARK"] != ""){
                        //全角アルファベットの可能性があるので半角に変換したい
                        $rMark = mb_convert_kana($row["R_VISION_MARK"], "r");
                        if(array_key_exists($rMark, $vision)){
                            $data["児童生徒健康診断票情報"]["健康診断一般"][$medCnt]["視力"]["矯正"]["右"] = $vision[$rMark];
                        }else{
                            $data["児童生徒健康診断票情報"]["健康診断一般"][$medCnt]["視力"]["矯正"]["右"] = "9";   //測定不能
                        }
                    }else{
                        $data["児童生徒健康診断票情報"]["健康診断一般"][$medCnt]["視力"]["矯正"]["右"] = "5";   //未検査
                    }
                    if($row["L_VISION_MARK"] != ""){
                        //全角アルファベットの可能性があるので半角に変換したい
                        $lMark = mb_convert_kana($row["L_VISION_MARK"], "r");
                        if(array_key_exists($lMark, $vision)){
                            $data["児童生徒健康診断票情報"]["健康診断一般"][$medCnt]["視力"]["矯正"]["左"] = $vision[$lMark];
                        }else{
                            $data["児童生徒健康診断票情報"]["健康診断一般"][$medCnt]["視力"]["矯正"]["左"] = "9";   //測定不能
                        }
                    }else{
                        $data["児童生徒健康診断票情報"]["健康診断一般"][$medCnt]["視力"]["矯正"]["左"] = "5";   //未検査
                    }
                }
                
                //眼の疾病及び異常
                if($row["EYEDISEASECD"] != ""){
                    if($row["EYEDISEASECD"] == "01"){
                        $data["児童生徒健康診断票情報"]["健康診断一般"][$medCnt]["眼の疾病及び異常"]["異常の有無"] = "0";
                    }else{
                        $data["児童生徒健康診断票情報"]["健康診断一般"][$medCnt]["眼の疾病及び異常"]["異常の有無"] = "1";
                        $data["児童生徒健康診断票情報"]["健康診断一般"][$medCnt]["眼の疾病及び異常"]["外字疾病名"] = "";
                        if($row["EYE_TEST_RESULT"] != ""){
                            $data["児童生徒健康診断票情報"]["健康診断一般"][$medCnt]["眼の疾病及び異常"]["内字疾病名"] = $row["EYE_TEST_RESULT"];
                        }else{
                            $data["児童生徒健康診断票情報"]["健康診断一般"][$medCnt]["眼の疾病及び異常"]["内字疾病名"] = $row["EYEDISEASE"];
                        }
                    }
                }else{
                    $data["児童生徒健康診断票情報"]["健康診断一般"][$medCnt]["眼の疾病及び異常"]["異常の有無"] = "2";
                }
                
                //聴力
                if($row["R_EAR"] != "" || $row["L_EAR"] != ""){
                    if($row["R_EAR"] != ""){
                        if($row["R_EAR"] == "01"){
                            $data["児童生徒健康診断票情報"]["健康診断一般"][$medCnt]["聴力"]["右"]["結果"] = "0";
                        }else{
                            $data["児童生徒健康診断票情報"]["健康診断一般"][$medCnt]["聴力"]["右"]["結果"] = "1";
                            if($row["R_EAR_DB"] != ""){
                                $data["児童生徒健康診断票情報"]["健康診断一般"][$medCnt]["聴力"]["右"]["聴力レベル"] = $row["R_EAR_DB"];
                            }
                        }
                    }else{
                        $data["児童生徒健康診断票情報"]["健康診断一般"][$medCnt]["聴力"]["右"]["結果"] = "2";
                    }
                    
                    if($row["L_EAR"] != ""){
                        if($row["L_EAR"] == "01"){
                            $data["児童生徒健康診断票情報"]["健康診断一般"][$medCnt]["聴力"]["左"]["結果"] = "0";
                        }else{
                            $data["児童生徒健康診断票情報"]["健康診断一般"][$medCnt]["聴力"]["左"]["結果"] = "1";
                            if($row["L_EAR_DB"] != ""){
                                $data["児童生徒健康診断票情報"]["健康診断一般"][$medCnt]["聴力"]["左"]["聴力レベル"] = $row["L_EAR_DB"];
                            }
                        }
                    }else{
                        $data["児童生徒健康診断票情報"]["健康診断一般"][$medCnt]["聴力"]["左"]["結果"] = "2";
                    }
                }
                
                //耳鼻咽頭疾患  疾病及び異常があるものとして作成する
                if($row["NOSEDISEASECD"] != ""){
                    if($row["NOSEDISEASECD"] == "01"){
                        $data["児童生徒健康診断票情報"]["健康診断一般"][$medCnt]["耳鼻咽頭疾患"]["疾病及び異常"]["異常の有無"] = "0";
                    }else{
                        $data["児童生徒健康診断票情報"]["健康診断一般"][$medCnt]["耳鼻咽頭疾患"]["疾病及び異常"]["異常の有無"] = "1";
                        $data["児童生徒健康診断票情報"]["健康診断一般"][$medCnt]["耳鼻咽頭疾患"]["疾病及び異常"]["外字疾病名"] = "";
                        if($row["NOSEDISEASECD_REMARK"] != ""){
                            $data["児童生徒健康診断票情報"]["健康診断一般"][$medCnt]["耳鼻咽頭疾患"]["疾病及び異常"]["内字疾病名"] = $row["NOSEDISEASECD_REMARK"];
                        }else{
                            $data["児童生徒健康診断票情報"]["健康診断一般"][$medCnt]["耳鼻咽頭疾患"]["疾病及び異常"]["内字疾病名"] = $row["NOSEDISEASE"];
                        }
                    }
                }else{
                    $data["児童生徒健康診断票情報"]["健康診断一般"][$medCnt]["耳鼻咽頭疾患"]["疾病及び異常"]["異常の有無"] = "2";   //未検査
                }
                //耳鼻咽頭それぞれのタグは作成しない
                
                //皮膚疾患
                if($row["SKINDISEASECD"] != ""){
                    if($row["SKINDISEASECD"] == "01"){
                        $data["児童生徒健康診断票情報"]["健康診断一般"][$medCnt]["皮膚疾患"]["異常の有無"] = "0";
                    }else{
                        $data["児童生徒健康診断票情報"]["健康診断一般"][$medCnt]["皮膚疾患"]["異常の有無"] = "1";
                        $data["児童生徒健康診断票情報"]["健康診断一般"][$medCnt]["皮膚疾患"]["外字疾病名"] = "";
                        $data["児童生徒健康診断票情報"]["健康診断一般"][$medCnt]["皮膚疾患"]["内字疾病名"] = $row["SKINDISEASE"];
                    }
                }else{
                    $data["児童生徒健康診断票情報"]["健康診断一般"][$medCnt]["皮膚疾患"]["異常の有無"] = "2";   //未検査
                }
                
                //結核
                if($row["TB_NAMECD"] != ""){
                    if($row["TB_NAMECD"] == "01"){
                        $data["児童生徒健康診断票情報"]["健康診断一般"][$medCnt]["結核"]["疾病及び異常"]["異常の有無"] = "0";
                    }else{
                        $data["児童生徒健康診断票情報"]["健康診断一般"][$medCnt]["結核"]["疾病及び異常"]["異常の有無"] = "1";
                        $data["児童生徒健康診断票情報"]["健康診断一般"][$medCnt]["結核"]["疾病及び異常"]["外字疾病名"] = "";
                        $data["児童生徒健康診断票情報"]["健康診断一般"][$medCnt]["結核"]["疾病及び異常"]["内字疾病名"] = $row["TB_NAME"];
                    }
                }else{
                    $data["児童生徒健康診断票情報"]["健康診断一般"][$medCnt]["結核"]["疾病及び異常"]["異常の有無"] = "2";
                }
                if($row["TB_ADVISE"] != ""){
                    //変換なぞ
                    $data["児童生徒健康診断票情報"]["健康診断一般"][$medCnt]["結核"]["指導区分"] = $row["TB_ADVISE"];    //適当
                }else{
                    $data["児童生徒健康診断票情報"]["健康診断一般"][$medCnt]["結核"]["指導区分"] = "00";    //空白
                }
                
                //心臓
                $data["児童生徒健康診断票情報"]["健康診断一般"][$medCnt]["心臓"]["外字臨床医学的検査"] = "";
                $data["児童生徒健康診断票情報"]["健康診断一般"][$medCnt]["心臓"]["内字臨床医学的検査"] = $row["HEART_MEDEXAM_NAME"];
                
                if($row["HEARTDISEASECD"] != ""){
                    if($row["HEARTDISEASECD"] == "01"){
                        $data["児童生徒健康診断票情報"]["健康診断一般"][$medCnt]["心臓"]["疾病及び異常"]["異常の有無"] = "0";
                    }else{
                        $data["児童生徒健康診断票情報"]["健康診断一般"][$medCnt]["心臓"]["疾病及び異常"]["異常の有無"] = "1";
                        $data["児童生徒健康診断票情報"]["健康診断一般"][$medCnt]["心臓"]["疾病及び異常"]["外字疾病名"] = "";
                        if($row["HEARTDISEASECD_REMARK"] != ""){
                            $data["児童生徒健康診断票情報"]["健康診断一般"][$medCnt]["心臓"]["疾病及び異常"]["内字疾病名"] = $row["HEARTDISEASECD_REMARK"];
                        }else{
                            $data["児童生徒健康診断票情報"]["健康診断一般"][$medCnt]["心臓"]["疾病及び異常"]["内字疾病名"] = $row["HEARTDISEASE"];
                        }
                    }
                }else{
                    $data["児童生徒健康診断票情報"]["健康診断一般"][$medCnt]["心臓"]["疾病及び異常"]["異常の有無"] = "2";
                }
                
                //尿
                $query = knjo132Query::getCode("06");
                $uricResult = $db->query($query);
                $uric = array();
                while($uricRow = $uricResult->fetchRow(DB_FETCHMODE_ASSOC)){
                    $uric[$uricRow["KENJA_CODE"]] = $uricRow["DICTIONARY_CODE"];
                }
                /*$uric = array("01" => "0",
                              "05" => "1",
                              "02" => "2",
                              "03" => "2",
                              "04" => "2");*/
                $query = knjo132Query::getUricMark();
                $urmarkResult = $db->query($query);
                $uricMark = array();
                while($urRow = $urmarkResult->fetchRow(DB_FETCHMODE_ASSOC)){
                    $uricMark[$urRow["KENJA_CODE"]] = $urRow["CODENAME"];
                }
                /*$uricMark = array("01" => "-",
                                  "02" => "+",
                                  "03" => "++",
                                  "04" => "+++",
                                  "05" => "±");*/

                if($row["ALBUMINURIA1CD"] != ""){
                    $data["児童生徒健康診断票情報"]["健康診断一般"][$medCnt]["尿"]["蛋白第1次"] = $uric[$row["ALBUMINURIA1CD"]];
                }else{
                    $data["児童生徒健康診断票情報"]["健康診断一般"][$medCnt]["尿"]["蛋白第1次"] = "3";
                }
                if($row["URICSUGAR1CD"] != ""){
                    $data["児童生徒健康診断票情報"]["健康診断一般"][$medCnt]["尿"]["糖第1次"] = $uric[$row["URICSUGAR1CD"]];
                }else{
                    $data["児童生徒健康診断票情報"]["健康診断一般"][$medCnt]["尿"]["糖第1次"] = "3";
                }
                if($row["URICBLEED1CD"] != ""){
                    $data["児童生徒健康診断票情報"]["健康診断一般"][$medCnt]["尿"]["潜血第1次"] = $uric[$row["URICBLEED1CD"]];
                }else{
                    $data["児童生徒健康診断票情報"]["健康診断一般"][$medCnt]["尿"]["潜血第1次"] = "3";
                }
                //その他の検査(第2次の結果)
                $uricOther = "";
                $uricCnm = "";
                if($row["ALBUMINURIA2CD"] != ""){
                    $uricOther = "蛋白：".$uricMark[$row["ALBUMINURIA2CD"]];
                    $uricCnm = "　";
                }
                if($row["URICSUGAR2CD"] != ""){
                    $uricOther .= $uricCnm."糖：".$uricMark[$row["URICSUGAR2CD"]];
                    $uricCnm = "　";
                }
                if($row["URICBLEED2CD"] != ""){
                    $uricOther .= $uricCnm."潜血：".$uricMark[$row["URICBLEED2CD"]];
                }
                if($uricOther != ""){
                    $data["児童生徒健康診断票情報"]["健康診断一般"][$medCnt]["尿"]["その他の検査"] = "尿2次_".$uricOther;
                //その他の検査項目を追加2020-01～タグ最大1なので条件分岐
                }elseif($row["URICOTHERTEST"] != ""){
                    $data["児童生徒健康診断票情報"]["健康診断一般"][$medCnt]["尿"]["その他の検査"] = $row["URICOTHERTEST"];
                }

                //寄生虫卵
                if($row["PARASITE"] != ""){
                    if($row["PARASITE"] == "01"){
                        $data["児童生徒健康診断票情報"]["健康診断一般"][$medCnt]["寄生虫卵"]["異常の有無"] = "0";
                    }elseif($row["PARASITE"] == "02"){
                        $data["児童生徒健康診断票情報"]["健康診断一般"][$medCnt]["寄生虫卵"]["異常の有無"] = "1";
                    }else{
                        $data["児童生徒健康診断票情報"]["健康診断一般"][$medCnt]["寄生虫卵"]["異常の有無"] = "2";
                    }
                }
                //その他の疾病及び異常
                if($row["OTHERDISEASECD"] != ""){
                    if($row["OTHERDISEASECD"] == "01"){
                        $data["児童生徒健康診断票情報"]["健康診断一般"][$medCnt]["その他の疾病及び異常"]["異常の有無"] = "0";
                    }else{
                        $data["児童生徒健康診断票情報"]["健康診断一般"][$medCnt]["その他の疾病及び異常"]["異常の有無"] = "1";
                        $data["児童生徒健康診断票情報"]["健康診断一般"][$medCnt]["その他の疾病及び異常"]["外字疾病名"] = "";
                        $data["児童生徒健康診断票情報"]["健康診断一般"][$medCnt]["その他の疾病及び異常"]["内字疾病名"] = $row["OTHERDISEASE"];
                    }
                }else{
                    $data["児童生徒健康診断票情報"]["健康診断一般"][$medCnt]["その他の疾病及び異常"]["異常の有無"] = "2";
                }
                
                //学校医
                if($row["DOC_CD"] != "" || $row["DOC_DATE"] != ""){
                    if($row["DOC_CD"] != ""){
                        $data["児童生徒健康診断票情報"]["健康診断一般"][$medCnt]["学校医"]["所見"] = $row["DOCMESS"];
                    }else{
                        $data["児童生徒健康診断票情報"]["健康診断一般"][$medCnt]["学校医"]["所見"] = mb_substr($row["DOC_REMARK"], 0, 25);
                    }
                    $data["児童生徒健康診断票情報"]["健康診断一般"][$medCnt]["学校医"]["年月日"] = $row["DOC_DATE"];
                }
                
                //事後措置
                $query = knjo132Query::getNameSpare2("F150");
                $spareResult = $db->getRow($query, DB_FETCHMODE_ASSOC);
                if($row["TREATCD"] != ""){
                    if($row["TREATCD"] != $spareResult["NAMECD2"]){
                        $data["児童生徒健康診断票情報"]["健康診断一般"][$medCnt]["事後措置"] = $row["TREAT"];
                    }elseif($row["TREATREMARK"] != ""){
                        $data["児童生徒健康診断票情報"]["健康診断一般"][$medCnt]["事後措置"] = $row["TREATREMARK"];
                    }
                }

                //備考
                if($row["REMARK"] != ""){
                    $data["児童生徒健康診断票情報"]["健康診断一般"][$medCnt]["備考"] = mb_substr($row["REMARK"],0,25);
                }
                
                $medCnt++;
            }
            
        }



        //歯科検査
        $query = knjo132Query::getTooth($this->SCHREGNO, "1");
        $cnt = $db->getOne($query);
        if($cnt > 0){
            $query = knjo132Query::getTooth($this->SCHREGNO);
            $result = $db->query($query);
            
            $tooCnt = 0;
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                if($tooCnt > 8){
                    break;
                }
                
                //年度
                $data["児童生徒健康診断票情報"]["歯科検査"][$tooCnt]["年度"] = $row["YEAR"];
                
                //年齢計算
                $april = $row["YEAR"]."0401";
                $age = floor(($april - $this->birthday)/10000);
                $data["児童生徒健康診断票情報"]["歯科検査"][$tooCnt]["年齢"] = $age;
                
                //歯列咬合と顎関節のコード取得
                $query = knjo132Query::getCode("07");
                $jResult = $db->query($query);
                $joint = array();
                while($jRow = $jResult->fetchRow(DB_FETCHMODE_ASSOC)){
                    $joint[$jRow["KENJA_CODE"]] = $jRow["DICTIONARY_CODE"];
                }
                
                //歯列咬合
                if($row["JAWS_JOINTCD"] != ""){
                    $data["児童生徒健康診断票情報"]["歯科検査"][$tooCnt]["歯列咬合"] = $joint[$row["JAWS_JOINTCD"]];
                }
                
                //顎関節
                if($row["JAWS_JOINTCD2"] != ""){
                    $data["児童生徒健康診断票情報"]["歯科検査"][$tooCnt]["顎関節"] = $joint[$row["JAWS_JOINTCD2"]];
                }
                
                //歯垢の状態
                $query = knjo132Query::getCode("08");
                $plResult = $db->query($query);
                $plaque = array();
                while($plRow = $plResult->fetchRow(DB_FETCHMODE_ASSOC)){
                    $plaque[$plRow["KENJA_CODE"]] = $plRow["DICTIONARY_CODE"];
                }
                if($row["PLAQUECD"] != ""){
                    $data["児童生徒健康診断票情報"]["歯科検査"][$tooCnt]["歯垢の状態"] = $plaque[$row["PLAQUECD"]];
                }
                
                //歯肉の状態
                $query = knjo132Query::getCode("09");
                $gumResult = $db->query($query);
                $gum = array();
                while($gumRow = $gumResult->fetchRow(DB_FETCHMODE_ASSOC)){
                    $gum[$gumRow["KENJA_CODE"]] = $gumRow["DICTIONARY_CODE"];
                }
                
                if($row["GUMCD"] != ""){
                    if(array_key_exists($row["GUMCD"], $gum)){
                        $data["児童生徒健康診断票情報"]["歯科検査"][$tooCnt]["歯肉の状態"] = $gum[$row["GUMCD"]];
                    }
                }
                
                $place = array("UP_R", "LW_R", "UP_L", "LW_L");
                $placeName = array("右上", "右下", "左上", "左下");
                $alpha = array("", "A", "B", "C", "D", "E");
                //歯の状態の配列
                $query = knjo132Query::getCode("11");
                $stResult = $db->query($query);
                $state = array();
                while($stRow = $stResult->fetchRow(DB_FETCHMODE_ASSOC)){
                    $state[$stRow["KENJA_CODE"]] = $stRow["DICTIONARY_CODE"];
                }
                //補助記号
                $query = knjo132Query::getCode("12");
                $supResult = $db->query($query);
                $support = array();
                while($supRow = $supResult->fetchRow(DB_FETCHMODE_ASSOC)){
                    $support[$supRow["KENJA_CODE"]] = $supRow["DICTIONARY_CODE"];
                }
                //歯式乳歯
                if($row["BABYTOOTH"] != 0 && $row["BABYTOOTH"] != ""){
                    //データが入ってなかったら現在歯の有無を2にする
                    for($i=0;$i<4;$i++){
                        for($j=1;$j<6;$j++){
                            if($row[$place[$i]."_BABY".$j] != ""){
                                $data["児童生徒健康診断票情報"]["歯科検査"][$tooCnt]["歯式乳歯"][$placeName[$i]]["乳歯".$alpha[$j]]["現在歯の有無"] = "1";
                                $data["児童生徒健康診断票情報"]["歯科検査"][$tooCnt]["歯式乳歯"][$placeName[$i]]["乳歯".$alpha[$j]]["歯の状態"] = $state[$row[$place[$i]."_BABY".$j]];
                                if(array_key_exists($row[$place[$i]."_BABY".$j], $support) && $row[$place[$i]."_BABY".$j] != "01"){
                                    $data["児童生徒健康診断票情報"]["歯科検査"][$tooCnt]["歯式乳歯"][$placeName[$i]]["乳歯".$alpha[$j]]["補助記号"] = $support[$row[$place[$i]."_BABY".$j]];
                                }
                                
                            }else{
                                $data["児童生徒健康診断票情報"]["歯科検査"][$tooCnt]["歯式乳歯"][$placeName[$i]]["乳歯".$alpha[$j]]["現在歯の有無"] = "2";
                            }
                        }
                    }
                }
                
                //歯式永久歯
                if($row["ADULTTOOTH"] != 0 && $row["ADULTTOOTH"] != ""){
                    for($i=0;$i<4;$i++){
                        for($j=1;$j<9;$j++){
                            if($row[$place[$i]."_ADULT".$j] != ""){
                                $data["児童生徒健康診断票情報"]["歯科検査"][$tooCnt]["歯式永久歯"][$placeName[$i]]["永久歯".$j]["現在歯の有無"] = "1";
                                $data["児童生徒健康診断票情報"]["歯科検査"][$tooCnt]["歯式永久歯"][$placeName[$i]]["永久歯".$j]["歯の状態"] = $state[$row[$place[$i]."_ADULT".$j]];
                                if(array_key_exists($row[$place[$i]."_ADULT".$j], $support) && $row[$place[$i]."_ADULT".$j] != "01"){
                                    $data["児童生徒健康診断票情報"]["歯科検査"][$tooCnt]["歯式永久歯"][$placeName[$i]]["永久歯".$j]["補助記号"] = $support[$row[$place[$i]."_ADULT".$j]];
                                }
                            }else{
                                $data["児童生徒健康診断票情報"]["歯科検査"][$tooCnt]["歯式永久歯"][$placeName[$i]]["永久歯".$j]["現在歯の有無"] = "2";
                            }
                        }
                    }
                }
                
                
                //乳歯の本数
                if($row["BABYTOOTH"] != 0 && $row["BABYTOOTH"] != ""){
                    $data["児童生徒健康診断票情報"]["歯科検査"][$tooCnt]["乳歯の本数"]["現在歯数"] = $row["BABYTOOTH"];
                    $data["児童生徒健康診断票情報"]["歯科検査"][$tooCnt]["乳歯の本数"]["未処置歯数"] = $row["REMAINBABYTOOTH"];
                    $data["児童生徒健康診断票情報"]["歯科検査"][$tooCnt]["乳歯の本数"]["処置歯数"] = $row["TREATEDBABYTOOTH"];
                }
                
                //永久歯の本数
                if($row["ADULTTOOTH"] != 0 && $row["ADULTTOOTH"] != ""){
                    $data["児童生徒健康診断票情報"]["歯科検査"][$tooCnt]["永久歯の本数"]["現在歯数"] = $row["ADULTTOOTH"];
                    $data["児童生徒健康診断票情報"]["歯科検査"][$tooCnt]["永久歯の本数"]["未処置歯数"] = $row["REMAINADULTTOOTH"];
                    $data["児童生徒健康診断票情報"]["歯科検査"][$tooCnt]["永久歯の本数"]["処置歯数"] = $row["TREATEDADULTTOOTH"];
                    $data["児童生徒健康診断票情報"]["歯科検査"][$tooCnt]["永久歯の本数"]["喪失歯数"] = $row["LOSTADULTTOOTH"];
                }
                
                //その他の疾病及び異常
                if($row["OTHERDISEASECD"] != ""){
                    if($row["OTHERDISEASECD"] == "01"){
                        $data["児童生徒健康診断票情報"]["歯科検査"][$tooCnt]["その他の疾病及び異常"]["異常の有無"] = "0";
                    }else{
                        $data["児童生徒健康診断票情報"]["歯科検査"][$tooCnt]["その他の疾病及び異常"]["異常の有無"] = "1";
                        $data["児童生徒健康診断票情報"]["歯科検査"][$tooCnt]["その他の疾病及び異常"]["外字疾病名"] = "";
                        if($row["OTHERDISEASE"] != ""){
                            $data["児童生徒健康診断票情報"]["歯科検査"][$tooCnt]["その他の疾病及び異常"]["内字疾病名"] = $row["OTHERDISEASE"];
                        }else{
                            $data["児童生徒健康診断票情報"]["歯科検査"][$tooCnt]["その他の疾病及び異常"]["内字疾病名"] = $row["OTHERDISEASENAME"];
                        }
                    }
                }else{
                    $data["児童生徒健康診断票情報"]["歯科検査"][$tooCnt]["その他の疾病及び異常"]["異常の有無"] = "2";
                }
                
                //学校歯科医
                $query = knjo132Query::getNameSpare2("F540");
                $spareResult = $db->getRow($query, DB_FETCHMODE_ASSOC);
                if($row["DENTISTREMARKCD"] != "" || $row["DENTISTREMARKDATE"] != ""){
                    if($row["DENTISTREMARKCD"] == $spareResult["NAMECD2"] && $row["DENTISTREMARK"] != ""){
                        $data["児童生徒健康診断票情報"]["歯科検査"][$tooCnt]["学校歯科医"]["所見"] = $row["DENTISTREMARK"];
                    }else{
                        $data["児童生徒健康診断票情報"]["歯科検査"][$tooCnt]["学校歯科医"]["所見"] = $row["DENTISTREMARKNAME"];
                    }
                    $data["児童生徒健康診断票情報"]["歯科検査"][$tooCnt]["学校歯科医"]["年月日"] = $row["DENTISTREMARKDATE"];
                }
                
                //事後措置
                $query = knjo132Query::getNameSpare2("F541");
                $spareResult = $db->getRow($query, DB_FETCHMODE_ASSOC);
                if($row["DENTISTTREATCD"] != ""){
                    if($row["DENTISTTREATCD"] != $spareResult["NAMECD2"]){
                        $data["児童生徒健康診断票情報"]["歯科検査"][$tooCnt]["事後措置"] = $row["DENTISTTREATNAME"];
                    }elseif($row["DENTISTTREAT"] != ""){
                        $data["児童生徒健康診断票情報"]["歯科検査"][$tooCnt]["事後措置"] = $row["DENTISTTREAT"];
                    }
                }

                $tooCnt++;
            }
            
        }
        
        
        //送信日時
        $data["送信日時"] = date("c");


        //DB切断
        Query::dbCheckIn($db);



        //初期値
        $count = 0;
        $past = "message";
        
        //functionに送る
        knjo132Model::create($data, $count, $past, $this);

        
        /*new dBug($this->new);
        exit;*/

        //タイトル部分 外から渡してこれるように
        $elementName = "児童生徒健康診断票情報メッセージ";
        $attribute = array("xmlns" => "urn:applic:xmlns:pf:edu:schema:2020-01",
                           "xmlns:xsi" => "http://www.w3.org/2001/XMLSchema-instance",
                           "xmlns:lgxml" => "urn:applic:xmlns:pf:lgxml:schema:2020-01",
                           "xsi:schemaLocation" => "urn:applic:xmlns:pf:edu:schema:2020-01 eduAK0201s-2020-01.xsd");


        //出力
        $today = date("Ymd");
        $time = date("His");
        $fileName = $this->schoolName."_".$this->SCHREGNO."_H".$today.$time.'.xml';  //  ファイル名
         
        $this->dom = new DomDocument('1.0');  // DOMを作成
        $this->dom->encoding = "UTF-8"; // 文字コードをUTF-8に
        $this->dom->formatOutput = true; // 出力XMLを整形(改行,タブ)する
        
        /*$this->message = $this->dom->appendChild($this->dom->createElement('指導要録情報メッセージ'));
        //$this->message = $this->dom->appendChild($this->dom->createElement('児童生徒健康診断票情報メッセージ'));
        $this->message->setAttribute("xmlns", "urn:applic:xmlns:pf:edu:schema:2016-01");
        $this->message->setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
        $this->message->setAttribute("xsi:schemaLocation", "urn:applic:xmlns:pf:edu:schema:2016-01 eduAK3101s-2016-01.xsd");
        //$this->message->setAttribute("xsi:schemaLocation", "urn:applic:xmlns:pf:edu:schema:2016-01 eduAK3201s-2016-01.xsd");*/
        
        $this->message = $this->dom->appendChild($this->dom->createElement($elementName));
        foreach($attribute as $key => $val){
            $this->message->setAttribute($key, $val);
        }
        

        for($count = 1; $count<get_count($this->new)+1; $count++){
            knjo132Model::makeXML($this->new[$count], $this); 
        }
        
        
        // HTTPヘッダを出力して
        header("Content-disposition: attachment; filename=".mb_convert_encoding($fileName, 'SJIS-win', 'UTF-8'));
        header("Content-type: application/octet-stream; name=" . $fileName);
         
        // DomXMLをXML形式で出力
        echo $this->dom->saveXML();

        $flg = true;
        //Query::dbCheckIn($db);

        if ($flg) {
            return true;
        } else {
            $this->setWarning("MSG303");    //データは存在していません。
            return false;
        }
        
    }

    function makeXML($array, &$make) {
        foreach($array as $key => $val){
            if(is_array($val)){
                foreach($val as $key2 => $val2){
                    $make->{$key2} = $make->{$key}->appendChild($make->dom->createElement("{$val2}"));
                }
            }else{
                $make->{$key}->appendChild($make->dom->createTextNode($array[$key]));
            }
        }
        
    }

    function create(&$array, $count, $past, &$data){
        $count++;
        
        foreach($array as $key => $val){
            $i = get_count($data->new[$count]);
            
            if(is_numeric($key)){
                
                if($key != "0"){
                    //ひとつ上の親になる部分の配列増やしたい
                    //$pastの後ろの数字が何文字かカウントする
                    $numCnt = 0;
                    for($j = 1; $j<mb_strlen($past); $j++){
                        $a = -1*$j;
                        $back = mb_substr($past, $a, 1);
                        if(is_numeric($back)){
                            $numCnt++;
                        }else{
                            break;
                        }
                    }
                    
                    
                    //$pastの文字列を数字で区切る
                    $parent = preg_split("/[0-9]/", $past);
                    
                    $parent = array_filter($parent);
                    $parent = array_values($parent);
                    
                    //後ろから2番目の配列の中身の文字数を数える
                    $mojicnt = mb_strlen($parent[get_count($parent)-1]);
                    
                    //数えた文字数+1文字分後ろから削除した文字列を作成する
                    $parname = mb_substr($past, 0, mb_strlen($past)-$mojicnt-$numCnt);
                    
                    //配列のキーに数字を追加して被らないようなキーにする
                    $now = $past.$key;
                    
                    //親になるキーを作る
                    $data->new[$count-1][$parname][$now] = $data->new[$count-1][$parname][$past];
                    
                }else{
                    $now = $past;
                }
                
                //$count--;
            }else{
                $now = $past.$key.$i;
                $data->new[$count][$past][$now] = $key;
                
            }
            
            if(is_array($val)){
                knjo132Model::create($val, $count, $now, $data);
            }else{
                $count++;
                $i = get_count($data->new[$count]);
                
                $data->new[$count][$now] = $val;
                
                $count--;
            }
        }
        return;
    }


    function getProperties()
    {
        $this->properties = array();
        //初期値
        $this->properties["useSchregRegdHdat"]  = "";

        $arr_useUnAdminMenuPrgid = array();
        $arr_useSubMenuId = array();
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
        while ($line = fgets($fp,1024)) {
            foreach ($this->properties as $key => $value) {
                $pos = strpos($line, $key);
                if ($pos === false) {
                } else {
                    $retVal = str_replace($key." = ", "", $line);
                    $this->properties[$key] = str_replace("\r\n", "", $retVal);
                    if ($key == "useUnAdminMenuPrgid") {
                        $arr_useUnAdminMenuPrgid[] = str_replace("\r\n", "", $retVal);
                    }
                    if ($key == "useSubMenuId") {
                        $arr_useSubMenuId[] = str_replace("\r\n", "", $retVal);
                    }
                }
            }
        }
        fclose($fp);

        return $retVal;
    }
    


}

?>
