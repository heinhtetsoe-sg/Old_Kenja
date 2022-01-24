<?php
//require_once('dBug.php');175には存在しない模様
require_once('for_php7.php');

class knjo122Model extends Model {



    var $grade;
    var $cmd;
    var $examyear;
    var $recordTableDiv;
    var $schoolCd;
    var $getPrgId;  //コールされたか
    var $sendAuth;  //親画面の権限
    var $auth;      //権限


    function knjo122Model()
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
        $row = knjo122Query::getSchregregdData($this);
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
        $query = knjo122Query::getCheck($this->SCHREGNO);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        if($row["CNT"] == ""){
            $this->setMessage("対象のデータはありません。");
            return false;
        }else{
            if($row["SCHOOL_KIND"] != "H"){
                $this->SYUBETU = $row["SCHOOL_KIND"];
            }else{  //SCHOOL_KINDが高校だったらエラーにする
                $this->setMessage("年度対象外です。");
                return false;
            }
        }
        
        $data = array();
        
        //データ作成
        $data["利用業務ユニット"] = "AK01";
        
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
        $query = knjo122Query::getMotoSchool($this->SYUBETU);
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
        
        //指導要録情報 小学校P01　中学校J01
        /*$data["指導要録情報"]["指導要録種別"] = "J01";
        if($data["指導要録情報"]["指導要録種別"] == "P01"){     //小学校
            $this->SYUBETU = "P";
        }else if($data["指導要録情報"]["指導要録種別"] == "J01"){  //中学校
            $this->SYUBETU = "J";
        }*/
        if($this->SYUBETU == "P"){
            $data["指導要録情報"]["指導要録種別"] = "P01";
        }else{
            $data["指導要録情報"]["指導要録種別"] = "J01";
        }
        
        //指導要録情報＞学校変更状況　複数可だけど対象の年度のデータのみ出力する
        //学校変更状況＞年度・学年
        $Query = knjo122Query::getChangeYear($this->SCHREGNO, $this->SYUBETU);
        $Result = $db->query($Query);
        
        $changeCnt = 0;
        while($Row = $Result->fetchRow(DB_FETCHMODE_ASSOC)){

            $data["指導要録情報"]["学校変更状況"][$changeCnt]["年度"] = $Row["YEAR"];
            $data["指導要録情報"]["学校変更状況"][$changeCnt]["学年"] = $Row["GRADE_CD"];

            //学校変更状況＞校長氏名
            //STAFF_PRINCIPAL_HIST_DATにデータがあるかをカウントする
            $query = knjo122Query::getPrincipal($this->SYUBETU, $Row["YEAR"], "1");
            $cnt = $db->getOne($query);
            if($cnt > 0){
                //データをPRINCIPAL_HIST_DATに取りに行く
                $query = knjo122Query::getPrincipal($this->SYUBETU, $Row["YEAR"]);
                $result = $db->query($query);
                
                $count = 0;
                
                while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                    $data["指導要録情報"]["学校変更状況"][$changeCnt]["校長氏名"][$count]["lgxml:外字氏名"] = "";
                    $data["指導要録情報"]["学校変更状況"][$changeCnt]["校長氏名"][$count]["lgxml:内字氏名"] = $row["STAFFNAME"];
                    $data["指導要録情報"]["学校変更状況"][$changeCnt]["校長氏名"][$count]["lgxml:フリガナ"] = $row["STAFFNAME_KANA"];
                    
                    $count++;
                }
                
            }else{
                //データをCERTIF_SCHOOL_DATから取得
                if($this->SYUBETU == "P"){
                    $syubetucd = "116";
                }else{
                    $syubetucd = "108";
                }
                $query = knjo122Query::getCertif($syubetucd, $Row["YEAR"]);
                $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
                
                $data["指導要録情報"]["学校変更状況"][$changeCnt]["校長氏名"]["lgxml:外字氏名"] = "";
                $data["指導要録情報"]["学校変更状況"][$changeCnt]["校長氏名"]["lgxml:内字氏名"] = $row["PRINCIPAL_NAME"];
                $data["指導要録情報"]["学校変更状況"][$changeCnt]["校長氏名"]["lgxml:フリガナ"] = "";
            }
            
            //学校変更状況＞ホームルーム担任氏名
            //カウント
            $query = knjo122Query::getHRteacher($this->SCHREGNO, $Row["YEAR"]);
            $result = $db->query($query);
            
            $count = 0;
            
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $data["指導要録情報"]["学校変更状況"][$changeCnt]["学級担任者氏名"][$count]["lgxml:外字氏名"] = "";
                $data["指導要録情報"]["学校変更状況"][$changeCnt]["学級担任者氏名"][$count]["lgxml:内字氏名"] = $row["STAFFNAME"];
                $data["指導要録情報"]["学校変更状況"][$changeCnt]["学級担任者氏名"][$count]["lgxml:フリガナ"] = $row["STAFFNAME_KANA"];
                
                $count++;
            }
            ///参考様式_版
            $query = knjo122Query::getStyleVer($Row["YEAR"], $this->SYUBETU);
            $styleVer = $db->getOne($query);
            $data["指導要録情報"]["学校変更状況"][$changeCnt]["参考様式_版"] = $styleVer;

            $changeCnt++;
        }
        
        //指導要録情報＞学籍に関する記録
        //学籍に関する記録＞児童生徒＞氏名・通称名・性別・生年月日・現住所・国籍
        $query = knjo122Query::getSchregData($this->SCHREGNO);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        
        $data["指導要録情報"]["学籍に関する記録"]["児童生徒"]["氏名"]["lgxml:外字氏名"] = "";
        $data["指導要録情報"]["学籍に関する記録"]["児童生徒"]["氏名"]["lgxml:内字氏名"] = $row["NAME"];
        $data["指導要録情報"]["学籍に関する記録"]["児童生徒"]["氏名"]["lgxml:フリガナ"] = $row["NAME_KANA"];
        //[NAME_SHOW]が存在する場合のみ作成
        if($row["NAME_SHOW"] != ""){
            $data["指導要録情報"]["学籍に関する記録"]["児童生徒"]["通称名"]["lgxml:外字氏名"] = "";
            $data["指導要録情報"]["学籍に関する記録"]["児童生徒"]["通称名"]["lgxml:内字氏名"] = $row["NAME_SHOW"];
            $data["指導要録情報"]["学籍に関する記録"]["児童生徒"]["通称名"]["lgxml:フリガナ"] = $row["NAME_KANA"];
        }
        $data["指導要録情報"]["学籍に関する記録"]["児童生徒"]["性別"] = $row["SEX"];
        $data["指導要録情報"]["学籍に関する記録"]["児童生徒"]["生年月日"] = $row["BIRTHDAY"];
        $data["指導要録情報"]["学籍に関する記録"]["児童生徒"]["現住所"]["住所"]["lgxml:住所コード"] = "";
        $data["指導要録情報"]["学籍に関する記録"]["児童生徒"]["現住所"]["住所"]["lgxml:外字住所"] = "";
        $data["指導要録情報"]["学籍に関する記録"]["児童生徒"]["現住所"]["住所"]["lgxml:内字住所"] = $row["ADDR1"];
        $data["指導要録情報"]["学籍に関する記録"]["児童生徒"]["現住所"]["住所"]["lgxml:外字方書"] = "";
        if($row["ADDR_FLG"] == '1'){
            $data["指導要録情報"]["学籍に関する記録"]["児童生徒"]["現住所"]["住所"]["lgxml:内字方書"] = $row["ADDR2"];
        }else{
            $data["指導要録情報"]["学籍に関する記録"]["児童生徒"]["現住所"]["住所"]["lgxml:内字方書"] = "";
        }
        $data["指導要録情報"]["学籍に関する記録"]["児童生徒"]["現住所"]["住所"]["lgxml:郵便番号"] = $row["ZIPCD"];
        if($row["NATION"] != ""){
            $data["指導要録情報"]["学籍に関する記録"]["児童生徒"]["国籍"] = $row["NATION"];
        }
        
        //学籍に関する記録＞児童生徒＞保護者
        $query = knjo122Query::getGuardianData($this->SCHREGNO);
        $result = $db->query($query);
        
        $count = 0;
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $data["指導要録情報"]["学籍に関する記録"]["児童生徒"]["保護者"][$count]["氏名"]["lgxml:外字氏名"] = "";
            $data["指導要録情報"]["学籍に関する記録"]["児童生徒"]["保護者"][$count]["氏名"]["lgxml:内字氏名"] = $row["GUARD_NAME"];
            $data["指導要録情報"]["学籍に関する記録"]["児童生徒"]["保護者"][$count]["氏名"]["lgxml:フリガナ"] = $row["GUARD_KANA"];
            $data["指導要録情報"]["学籍に関する記録"]["児童生徒"]["保護者"][$count]["現住所"]["住所"]["lgxml:住所コード"] = "";
            $data["指導要録情報"]["学籍に関する記録"]["児童生徒"]["保護者"][$count]["現住所"]["住所"]["lgxml:外字住所"] = "";
            $data["指導要録情報"]["学籍に関する記録"]["児童生徒"]["保護者"][$count]["現住所"]["住所"]["lgxml:内字住所"] = $row["GUARD_ADDR1"];
            $data["指導要録情報"]["学籍に関する記録"]["児童生徒"]["保護者"][$count]["現住所"]["住所"]["lgxml:外字方書"] = "";
            $data["指導要録情報"]["学籍に関する記録"]["児童生徒"]["保護者"][$count]["現住所"]["住所"]["lgxml:内字方書"] = $row["GUARD_ADDR2"];
            $data["指導要録情報"]["学籍に関する記録"]["児童生徒"]["保護者"][$count]["現住所"]["住所"]["lgxml:郵便番号"] = $row["GUARD_ZIPCD"];
            
            $count++;
        }
        
        
        
        //学籍に関する記録＞在学状況＞学校情報(送信元情報と同じでOK)
        $query = knjo122Query::getMotoSchool($this->SYUBETU);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        
        $data["指導要録情報"]["学籍に関する記録"]["在学状況"]["学校情報"]["外字学校名"] = "";
        $data["指導要録情報"]["学籍に関する記録"]["在学状況"]["学校情報"]["内字学校名"] = $row["SCHOOLNAME1"];
        $data["指導要録情報"]["学籍に関する記録"]["在学状況"]["学校情報"]["所在地"]["lgxml:住所コード"] = "";
        $data["指導要録情報"]["学籍に関する記録"]["在学状況"]["学校情報"]["所在地"]["lgxml:外字住所"] = "";
        $data["指導要録情報"]["学籍に関する記録"]["在学状況"]["学校情報"]["所在地"]["lgxml:内字住所"] = $row["SCHOOLADDR1"];
        $data["指導要録情報"]["学籍に関する記録"]["在学状況"]["学校情報"]["所在地"]["lgxml:外字方書"] = "";
        $data["指導要録情報"]["学籍に関する記録"]["在学状況"]["学校情報"]["所在地"]["lgxml:内字方書"] = $row["SCHOOLADDR2"];
        $data["指導要録情報"]["学籍に関する記録"]["在学状況"]["学校情報"]["所在地"]["lgxml:郵便番号"] = $row["SCHOOLZIPCD"];
        if($row["SCHOOLTELNO"] != ""){
            $data["指導要録情報"]["学籍に関する記録"]["在学状況"]["学校情報"]["電話番号"] = $row["SCHOOLTELNO"];
        }
        
        //学籍に関する記録＞在学状況＞在学学年
        $query = knjo122Query::getZaigakuGrade($this->SCHREGNO, $this->SYUBETU);
        $result = $db->query($query);
        
        $count = 0;
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $data["指導要録情報"]["学籍に関する記録"]["在学状況"]["在学学年"][$count]["年度"] = $row["YEAR"];
            $data["指導要録情報"]["学籍に関する記録"]["在学状況"]["在学学年"][$count]["学年"] = $row["GRADE"];
            if($this->properties["useSchregRegdHdat"] != 0){
                if($row["HR_CLASS_NAME1"] != ""){
                    $data["指導要録情報"]["学籍に関する記録"]["在学状況"]["在学学年"][$count]["学級"] = $row["HR_CLASS_NAME1"];
                }else{
                    $data["指導要録情報"]["学籍に関する記録"]["在学状況"]["在学学年"][$count]["学級"] = $row["HR_CLASS"];
                }
            }else{
                if($row["NAME1"] != ""){
                    $data["指導要録情報"]["学籍に関する記録"]["在学状況"]["在学学年"][$count]["学級"] = $row["NAME1"];
                }else{
                    $data["指導要録情報"]["学籍に関する記録"]["在学状況"]["在学学年"][$count]["学級"] = $row["HR_CLASS"];
                }
            }
            $data["指導要録情報"]["学籍に関する記録"]["在学状況"]["在学学年"][$count]["整理番号"] = $row["ATTENDNO"];
            
            $count++;
        }
        
        //学籍に関する記録＞異動状況＞入学or編入学or転入学・転学or退学or卒業
        $query = knjo122Query::getIdou($this->SCHREGNO);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        
        if($row["FINSCHOOLCD"] != "" && $row["FINSCHOOL_NAME"] != ""){
            //入学前の経歴 卒業・卒園手打ち
            if($row["FINSCHOOL_TYPE"] == "1"){
                $data["指導要録情報"]["学籍に関する記録"]["異動状況"]["入学前の経歴"] = $row["FINSCHOOL_NAME"]."卒園";
            }else{
                $data["指導要録情報"]["学籍に関する記録"]["異動状況"]["入学前の経歴"] = $row["FINSCHOOL_NAME"]."卒業";
            }
        }
        if($row["ENT_DIV"] == "4"){
            //転入学
            $data["指導要録情報"]["学籍に関する記録"]["異動状況"]["転入学"]["転入学年月日"] = $row["ENT_DATE"];
            $data["指導要録情報"]["学籍に関する記録"]["異動状況"]["転入学"]["転入学時学年"] = $row["GRADE"];
            if($row["ENT_REASON"] != ""){
                $data["指導要録情報"]["学籍に関する記録"]["異動状況"]["転入学"]["備考"] = $row["ENT_REASON"];
            }
        }else if($row["ENT_DIV"] == "5"){
            //編入学
            $data["指導要録情報"]["学籍に関する記録"]["異動状況"]["編入学"]["編入学年月日"] = $row["ENT_DATE"];
            $data["指導要録情報"]["学籍に関する記録"]["異動状況"]["編入学"]["編入学時学年"] = $row["GRADE"];
            if($row["ENT_REASON"] != ""){
                $data["指導要録情報"]["学籍に関する記録"]["異動状況"]["編入学"]["備考"] = $row["ENT_REASON"];
            }
        }else{
            //入学
            $data["指導要録情報"]["学籍に関する記録"]["異動状況"]["入学"]["入学年月日"] = $row["ENT_DATE"];
            if($row["ENT_REASON"] != ""){
                $data["指導要録情報"]["学籍に関する記録"]["異動状況"]["入学"]["備考"] = $row["ENT_REASON"];
            }
        }
        if($row["GRD_DIV"] != ""){
            if($row["GRD_DIV"] == "1"){
                //卒業
                $data["指導要録情報"]["学籍に関する記録"]["異動状況"]["卒業"]["卒業年月日"] = $row["GRD_DATE"];
            }else if($row["GRD_DIV"] == "2"){
                //退学
                $data["指導要録情報"]["学籍に関する記録"]["異動状況"]["退学"]["退学年月日"] = $row["GRD_DATE"];
                if($row["GRD_REASON"] != ""){
                    $data["指導要録情報"]["学籍に関する記録"]["異動状況"]["退学"]["備考"] = $row["GRD_REASON"];
                }
            }else if($row["GRD_DIV"] == "3"){
                //転学
                $data["指導要録情報"]["学籍に関する記録"]["異動状況"]["転学"]["転学年月日"] = $row["BASE_REMARK1"];
                if($row["GRD_DATE"] != ""){
                    $data["指導要録情報"]["学籍に関する記録"]["異動状況"]["転学"]["学校を去った日"] = $row["GRD_DATE"];
                }
                if($row["GRD_REASON"] != ""){
                    $data["指導要録情報"]["学籍に関する記録"]["異動状況"]["転学"]["備考"] = $row["GRD_REASON"];
                }
            }
        }
        
        //学籍に関する記録＞異動状況＞進学先就職先等
        $query = knjo122Query::getGrdAftCnt($this->SCHREGNO);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        
        if($row["COUNT"] != "" && $row["SENKOU_KIND"] == "0"){
            //進学先
            $query = knjo122Query::getGrdAftSch($this->SCHREGNO);
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            if(mb_substr($row["STAT_CD"], 0, 7) != "9999999"){
                $data["指導要録情報"]["学籍に関する記録"]["異動状況"]["進学先就職先等"] = $row["SCHOOL_NAME"]." ".$row["FACULTYNAME"]." ".$row["DEPARTMENTNAME"];
            }else{
                $data["指導要録情報"]["学籍に関する記録"]["異動状況"]["進学先就職先等"] = $row["THINKEXAM"];
            }
            
        }else if($row["COUNT"] != "" && $row["SENKOU_KIND"] == "1"){
            //就職先
            $query = knjo122Query::getGrdAftComp($this->SCHREGNO);
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);

            if(mb_substr($row["STAT_CD"], 0, 7) != "9999999"){
                $data["指導要録情報"]["学籍に関する記録"]["異動状況"]["進学先就職先等"] = $row["COMPANY_NAME"];
            }else{
                $data["指導要録情報"]["学籍に関する記録"]["異動状況"]["進学先就職先等"] = $row["JOB_THINK"];
            }

        }
        
        //指導に関する記録＞学年・様式種別
        $query = knjo122Query::getGrade($this->SCHREGNO, $this->SYUBETU);
        $result = $db->query($query);

        $count = 0;
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $data["指導要録情報"]["指導に関する記録"][$count]["学年"] = $row["GRADE_CD"];
            if($row["HANDICAP"] == "002"){
                $data["指導要録情報"]["指導に関する記録"][$count]["様式種別"] = "02";
            }else if($row["HANDICAP"] == "003"){
                $data["指導要録情報"]["指導に関する記録"][$count]["様式種別"] = "03";
            }else{
                $data["指導要録情報"]["指導に関する記録"][$count]["様式種別"] = "01";
            }
            
            $ySyubetu = $data["指導要録情報"]["指導に関する記録"][$count]["様式種別"];
            

            ///参考様式_版取得 現状は特別な教育_道徳のみ利用している。
            $query = knjo122Query::getStyleVer($row["YEAR"], $this->SYUBETU);
            $styleVer = $db->getOne($query);
            // $data["指導要録情報"]["指導に関する記録"]["参考様式_版"] = $styleVer;

            if($ySyubetu != "03"){
                //各教科の学習の記録
                //観点別学習状況
                $query = knjo122Query::getPoint($row["YEAR"], $this->SCHREGNO, $this->SYUBETU);
                $pResult = $db->query($query);
                $pCnt = 0;
                while($pRow = $pResult->fetchRow(DB_FETCHMODE_ASSOC)){
                    //必修選択追加 1->必修教科 2->選択教科
                    if($pRow["ELECTDIV"] != ""){
                        if($pRow["ELECTDIV"] != "1"){
                            $data["指導要録情報"]["指導に関する記録"][$count]["各教科の学習の記録"]["観点別学習状況"][$pCnt]["必修選択"] = "1";
                        }else{
                            $data["指導要録情報"]["指導に関する記録"][$count]["各教科の学習の記録"]["観点別学習状況"][$pCnt]["必修選択"] = "2";
                        }
                    }
                    if($pRow["CLASSCD"] != ""){
                        $data["指導要録情報"]["指導に関する記録"][$count]["各教科の学習の記録"]["観点別学習状況"][$pCnt]["教科"] = $pRow["CLASSCD"];
                    }else{
                        $data["指導要録情報"]["指導に関する記録"][$count]["各教科の学習の記録"]["観点別学習状況"][$pCnt]["教科_その他"] = $pRow["CLASSNAME"];
                    }
                    
                    if($pRow["VIEWCODE"] != ""){
                        $data["指導要録情報"]["指導に関する記録"][$count]["各教科の学習の記録"]["観点別学習状況"][$pCnt]["観点"] = $pRow["VIEWCODE"];
                    }else{
                        $data["指導要録情報"]["指導に関する記録"][$count]["各教科の学習の記録"]["観点別学習状況"][$pCnt]["観点_その他"] = $pRow["VIEWNAME"];
                    }
                    if($pRow["STATUS"] != ""){
                        $data["指導要録情報"]["指導に関する記録"][$count]["各教科の学習の記録"]["観点別学習状況"][$pCnt]["評価"] = $pRow["STATUS"];
                    }else{
                        $data["指導要録情報"]["指導に関する記録"][$count]["各教科の学習の記録"]["観点別学習状況"][$pCnt]["評価"] = "-";
                    }
                    
                    $pCnt++;
                }
                //評定情報
                $query = knjo122Query::getCurriculumRating($row["YEAR"], $this->SCHREGNO, $this->SYUBETU);
                $crResult = $db->query($query);
                $crCnt = 0;
                while($crRow = $crResult->fetchRow(DB_FETCHMODE_ASSOC)){
                    //必修選択追加 1->必修教科 2->選択教科
                    if($crRow["ELECTDIV"] != ""){
                        if($crRow["ELECTDIV"] != "1"){
                            $data["指導要録情報"]["指導に関する記録"][$count]["各教科の学習の記録"]["評定情報"][$crCnt]["必修選択"] = "1";
                        }else{
                            $data["指導要録情報"]["指導に関する記録"][$count]["各教科の学習の記録"]["評定情報"][$crCnt]["必修選択"] = "2";
                        }
                    }
                    if($crRow["CLASSCD"] != ""){
                        $data["指導要録情報"]["指導に関する記録"][$count]["各教科の学習の記録"]["評定情報"][$crCnt]["教科"] = $crRow["CLASSCD"];
                    }else{
                        $data["指導要録情報"]["指導に関する記録"][$count]["各教科の学習の記録"]["評定情報"][$crCnt]["教科_その他"] = $crRow["CLASSNAME"];
                    }
                    if($crRow["VALUATION"] != ""){
                        $data["指導要録情報"]["指導に関する記録"][$count]["各教科の学習の記録"]["評定情報"][$crCnt]["評定"] = $crRow["VALUATION"];
                    }else{
                        $data["指導要録情報"]["指導に関する記録"][$count]["各教科の学習の記録"]["評定情報"][$crCnt]["評定"] = "";
                    }
                    $crCnt++;
                }
            }
            
            // 特別の教科_道徳　様式種別のチェックは不要か。様式種別_版はチェックする。
            $array = array("F02", "F03");
            if (in_array($styleVer, $array)) {
                //存在チェック
                $query = knjo122Query::getMoral($row["YEAR"], $this->SCHREGNO);
                $MoralResult = $db->getOne($query);
                if ($MoralResult != "") {
                    $data["指導要録情報"]["指導に関する記録"][$count]["特別の教科_道徳"]["評価"] = $MoralResult;
                }
            }
            
            if($this->SYUBETU == "P" && $ySyubetu != "03"){     //児童でかつ知的障害以外のときのみ
                //外国語活動の記録
                $query = knjo122Query::getForeignAct($row["YEAR"], $this->SCHREGNO, "1");
                $fCount = $db->getOne($query);
                
                if($fCount > 0){
                    $query = knjo122Query::getForeignAct($row["YEAR"], $this->SCHREGNO);
                    $fResult = $db->query($query);
                    $fCnt = 0;
                    while($fRow = $fResult->fetchRow(DB_FETCHMODE_ASSOC)){
                        if($fRow["FOREIGNLANGACT1"] != ""){
                            //FOREIGNLANGACT1が観点P100-101 -> 2020-01改修にて「001」へ変更
                            $data["指導要録情報"]["指導に関する記録"][$count]["外国語活動の記録"]["学習活動の状況"][$fCnt]["観点"] = "001";
                            $data["指導要録情報"]["指導に関する記録"][$count]["外国語活動の記録"]["学習活動の状況"][$fCnt]["評価"] = $fRow["FOREIGNLANGACT1"];
                            
                            $fCnt++;
                        }
                        if($fRow["FOREIGNLANGACT2"] != ""){
                            //FOREIGNLANGACT1が観点P100-101 -> 2020-01改修にて「002」へ変更
                            $data["指導要録情報"]["指導に関する記録"][$count]["外国語活動の記録"]["学習活動の状況"][$fCnt]["観点"] = "002";
                            $data["指導要録情報"]["指導に関する記録"][$count]["外国語活動の記録"]["学習活動の状況"][$fCnt]["評価"] = $fRow["FOREIGNLANGACT2"];
                            
                            $fCnt++;
                        }
                        if($fRow["FOREIGNLANGACT3"] != ""){
                            //FOREIGNLANGACT1が観点P100-101 -> 2020-01改修にて「003」へ変更
                            $data["指導要録情報"]["指導に関する記録"][$count]["外国語活動の記録"]["学習活動の状況"][$fCnt]["観点"] = "003";
                            $data["指導要録情報"]["指導に関する記録"][$count]["外国語活動の記録"]["学習活動の状況"][$fCnt]["評価"] = $fRow["FOREIGNLANGACT3"];
                            
                            $fCnt++;
                        }
                    }
                }
            }
            
            if($ySyubetu != "03"){
                //総合的な学習の時間
                //小学校はHTRAINREMARK_P_DATから
                //中学校はHTRAINREMARK_DATから取得
                if($this->SYUBETU != "P"){
                    $table = "HTRAINREMARK_DAT";
                }else{
                    $table = "HTRAINREMARK_P_DAT";
                }
                $query = knjo122Query::getIntegAct($row["YEAR"], $this->SCHREGNO, $table, "1");
                $iCount = $db->getOne($query);
                if($iCount > 0){
                    $query = knjo122Query::getIntegAct($row["YEAR"], $this->SCHREGNO, $table);
                    $iResult = $db->query($query);
                    $iCnt = 0;
                    
                    while($iRow = $iResult->fetchRow(DB_FETCHMODE_ASSOC)){
                        if($iRow["TOTALSTUDYACT"] != ""){
                            $data["指導要録情報"]["指導に関する記録"][$count]["総合的な学習の時間の記録"]["学習活動の状況"][$iCnt]["学習活動"] = $iRow["TOTALSTUDYACT"];
                        }
                        if($iRow["VIEWREMARK"] != ""){
                            //"/"でひとつの項目ずつに区切る
                            $viewremark = explode("/", $iRow["VIEWREMARK"]);
                            $vCnt = 0;
                            foreach($viewremark as $key => $val){
                                if($val != ""){
                                    $data["指導要録情報"]["指導に関する記録"][$count]["総合的な学習の時間の記録"]["学習活動の状況"][$iCnt]["観点"][$key] = $val;
                                    $vCnt++;
                                }
                            }
                        }
                        $data["指導要録情報"]["指導に関する記録"][$count]["総合的な学習の時間の記録"]["学習活動の状況"][$iCnt]["評価"] = $iRow["TOTALSTUDYVAL"];
                        
                        $iCnt++;
                    }
                }
                
                //特別活動の記録    BEHAVIOR_DATのDIV=4のデータを出力する   RECORD=1のデータのみ取得
                //小学校はHTRAINREMARK_P_DATを
                //中学校はHTRAINREMARK_DATをくっつける
                if($this->SYUBETU != "P"){
                    $table = "HTRAINREMARK_DAT";
                }else{
                    $table = "HTRAINREMARK_P_DAT";
                }
                $query = knjo122Query::getSpAct($row["YEAR"], $this->SCHREGNO, $table, $this->SYUBETU, "1");
                $spCount = $db->getOne($query);
                if($spCount > 0){
                    $query = knjo122Query::getSpAct($row["YEAR"], $this->SCHREGNO, $table, $this->SYUBETU);
                    $spResult = $db->query($query);
                    $spCnt = 0;
                    while($spRow = $spResult->fetchRow(DB_FETCHMODE_ASSOC)){
                        if($spRow["POINT"] != ""){
                            $data["指導要録情報"]["指導に関する記録"][$count]["特別活動の記録"]["活動の状況"][$spCnt]["内容"] = $spRow["POINT"];
                        }else{
                            $data["指導要録情報"]["指導に関する記録"][$count]["特別活動の記録"]["活動の状況"][$spCnt]["内容_その他"] = $spRow["NAME1"];
                        }
                        if($spRow["SPECIALACTREMARK"] != ""){
                            $data["指導要録情報"]["指導に関する記録"][$count]["特別活動の記録"]["活動の状況"][$spCnt]["観点"] = $spRow["SPECIALACTREMARK"];
                        }
                        //SQL結果にて手打ち
                        if($spRow["RECORD"] == "1" ){
                            $data["指導要録情報"]["指導に関する記録"][$count]["特別活動の記録"]["活動の状況"][$spCnt]["評価"] = "○";
                        }else{
                            $data["指導要録情報"]["指導に関する記録"][$count]["特別活動の記録"]["活動の状況"][$spCnt]["評価"] = "／";
                        }
                        
                        $spCnt++;
                    }
                }
            }
            
            //行動の記録
            if($ySyubetu != "03"){
                //行動の状況
                $query = knjo122Query::getBehavior($row["YEAR"], $this->SCHREGNO, $this->SYUBETU, "1");
                $bCount = $db->getOne($query);
                if($bCount > 0){
                    $query = knjo122Query::getBehavior($row["YEAR"], $this->SCHREGNO, $this->SYUBETU);
                    $bResult = $db->query($query);
                    $bCnt = 0;

                    while($bRow = $bResult->fetchRow(DB_FETCHMODE_ASSOC)){
                        if($bRow["POINT"] != ""){
                            $data["指導要録情報"]["指導に関する記録"][$count]["行動の記録"]["行動の状況"][$bCnt]["項目"] = $bRow["POINT"];
                        }else{
                            $data["指導要録情報"]["指導に関する記録"][$count]["行動の記録"]["行動の状況"][$bCnt]["項目_その他"] = $bRow["CODENAME"];
                        }
                        //SQL結果にて手打ち
                        if($bRow["RECORD"] == "1" ){
                            $data["指導要録情報"]["指導に関する記録"][$count]["行動の記録"]["行動の状況"][$bCnt]["評価"] = "○";
                        }else{
                            $data["指導要録情報"]["指導に関する記録"][$count]["行動の記録"]["行動の状況"][$bCnt]["評価"] = "／";
                        }
                        $bCnt++;
                    }
                }
            }else{
                //追加実装したが、テーブル・カラムともに確認必要
                $query = knjo122Query::getActScoreSp($this->SCHREGNO, $row["YEAR"]);
                $asSpResult = $db->getRow($query, DB_FETCHMODE_ASSOC);
                if($asSpResult["REMARK1"] != ""){
                    $data["指導要録情報"]["指導に関する記録"][$count]["行動の記録"]["評価"] = $asSpResult["REMARK1"];
                }
            }
            
            
            if($ySyubetu == "02"){
                //自立活動の記録
                //HTRAINREMARK_DETAIL2_DATが存在しないので分からない
                $query = knjo122Query::getActIndependence($this->SCHREGNO, $row["YEAR"]);
                $aiResult = $db->getRow($query, DB_FETCHMODE_ASSOC);
                if($aiResult["REMARK1"] != ""){
                    $data["指導要録情報"]["指導に関する記録"][$count]["自立活動の記録"]["自立の状況"] = $aiResult["REMARK1"];
                }
            }
            
            if($ySyubetu == "03"){
                //小学校はHTRAINREMARK_P_DATを
                //中学校はHTRAINREMARK_DATをくっつける
                if($this->SYUBETU != "P"){
                    $table = "HTRAINREMARK_DAT";
                }else{
                    $table = "HTRAINREMARK_P_DAT";
                }
                //各教科_特別活動_自立活動の記録
                $query = knjo122Query::getAllAct($row["YEAR"], $this->SCHREGNO, $table, $this->SYUBETU, "1");
                $allCount = $db->getOne($query);
                if($allCount > 0){
                    $query = knjo122Query::getAllAct($row["YEAR"], $this->SCHREGNO, $table, $this->SYUBETU);
                    $aResult = $db->query($query);
                    $aCnt = 0;
                    
                    while($aRow = $aResult->fetchRow(DB_FETCHMODE_ASSOC)){
                        //テストテーブルを代用して確認　修正必要
                        if($aRow["REMARK1"] != ""){
                            if($aRow["CLASSCD"] != ""){
                                $data["指導要録情報"]["指導に関する記録"][$count]["各教科_特別活動_自立活動の記録"]["状況"][$aCnt]["教科等"] = $aRow["CLASSCD"];
                            }else{
                                $data["指導要録情報"]["指導に関する記録"][$count]["各教科_特別活動_自立活動の記録"]["状況"][$aCnt]["教科等_その他"] = $aRow["CLASSNAME"];
                            }
                            $data["指導要録情報"]["指導に関する記録"][$count]["各教科_特別活動_自立活動の記録"]["状況"][$aCnt]["評価"] = $aRow["REMARK1"];
                            $aCnt++;
                        }
                    }
                }
            }
            
            //総合的所見及び指導上参考となる諸事項
            //小学校はHTRAINREMARK_P_DATを
            //中学校はHTRAINREMARK_DATをくっつける
            if($this->SYUBETU != "P"){
                $table = "HTRAINREMARK_DAT";
            }else{
                $table = "HTRAINREMARK_P_DAT";
            }
            $query = knjo122Query::getSyoken($row["YEAR"], $this->SCHREGNO, $table, "1");
            $sCount = $db->getOne($query);
            if($sCount > 0){
                $query = knjo122Query::getSyoken($row["YEAR"], $this->SCHREGNO, $table);
                $sRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
                
                $data["指導要録情報"]["指導に関する記録"][$count]["総合的所見及び指導上参考となる諸事項"]["事項"]["事項_事実_所見"] = $sRow["TOTALREMARK"];
            }
            
            
            //出欠の記録
            $query = knjo122Query::getAttend($row["YEAR"], $this->SCHREGNO, $table);
            $atRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
            
            $data["指導要録情報"]["指導に関する記録"][$count]["出欠の記録"]["授業日数"] = $atRow["CLASSDAYS"];
            $data["指導要録情報"]["指導に関する記録"][$count]["出欠の記録"]["出席停止_忌引等の日数"] = $atRow["STOPCNT"];
            if($atRow["MUSTCNT"] != ""){
                $data["指導要録情報"]["指導に関する記録"][$count]["出欠の記録"]["出席しなければならない日数"] = $atRow["MUSTCNT"];
            }
            $data["指導要録情報"]["指導に関する記録"][$count]["出欠の記録"]["欠席日数"] = $atRow["ABSECNT"];
            $data["指導要録情報"]["指導に関する記録"][$count]["出欠の記録"]["出席日数"] = $atRow["PRESENT"];
            if($atRow["ATTENDREC_REMARK"] != ""){
                $data["指導要録情報"]["指導に関する記録"][$count]["出欠の記録"]["備考"] = $atRow["ATTENDREC_REMARK"];
            }
                        
            $count++;
        }
        //入学時の障害の状態
        if($ySyubetu != "01"){
            //入学時の障害の状態
            //HTRAINREMARK_DETAIL2_DATがないので分からず
            $query = knjo122Query::getEntCondition($this->SCHREGNO);
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $data["指導要録情報"]["入学時の障害の状態"]["障害の状態"] = $row["REMARK1"];
        }

        
        //送信日時
        $data["送信日時"] = date("c");


        //DB切断
        Query::dbCheckIn($db);


        //初期値
        $count = 0;
        $past = "message";
        
        //functionに送る
        knjo122Model::create($data, $count, $past, $this);
//print_r($this->new);
//exit;
        
        /*new dBug($this->new);
        exit;*/

        //タイトル部分 外から渡してこれるように
        $elementName = "指導要録情報メッセージ";
        $attribute = array("xmlns" => "urn:applic:xmlns:pf:edu:schema:2020-01",
                           "xmlns:xsi" => "http://www.w3.org/2001/XMLSchema-instance",
                           "xmlns:lgxml" => "urn:applic:xmlns:pf:lgxml:schema:2020-01",
                           "xsi:schemaLocation" => "urn:applic:xmlns:pf:edu:schema:2020-01 eduAK0101s-2020-01.xsd");


        //出力
        $today = date("Ymd");
        $time = date("His");
        $fileName = $this->schoolName."_".$this->SCHREGNO."_S".$today.$time.'.xml';  //  ファイル名
         
        $this->dom = new DomDocument('1.0');  // DOMを作成
        $this->dom->encoding = "UTF-8"; // 文字コードをUTF-8に
        $this->dom->formatOutput = true; // 出力XMLを整形(改行,タブ)する
        
        /*$this->message = $this->dom->appendChild($this->dom->createElement('指導要録情報メッセージ'));
        //$this->message = $this->dom->appendChild($this->dom->createElement('生徒健康診断票情報メッセージ'));
        $this->message->setAttribute("xmlns", "urn:applic:xmlns:pf:edu:schema:2016-01");
        $this->message->setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
        $this->message->setAttribute("xsi:schemaLocation", "urn:applic:xmlns:pf:edu:schema:2016-01 eduAK3101s-2016-01.xsd");
        //$this->message->setAttribute("xsi:schemaLocation", "urn:applic:xmlns:pf:edu:schema:2016-01 eduAK3201s-2016-01.xsd");*/
        
        $this->message = $this->dom->appendChild($this->dom->createElement($elementName));
        foreach($attribute as $key => $val){
            $this->message->setAttribute($key, $val);
        }
        

        for($count = 1; $count<get_count($this->new)+1; $count++){
            knjo122Model::makeXML($this->new[$count], $this); 
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
                knjo122Model::create($val, $count, $now, $data);
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
