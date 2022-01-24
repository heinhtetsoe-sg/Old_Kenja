<?php
//require_once('dBug.php');
require_once('for_php7.php');

class knjo133Model extends Model {



    var $grade;
    var $cmd;
    var $examyear;
    var $recordTableDiv;
    var $schoolCd;
    var $getPrgId;  //コールされたか
    var $sendAuth;  //親画面の権限
    var $auth;      //権限


    function knjo133Model()
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
    }

    
    public function schregChk($schregno)
    {
        $row = knjo133Query::getSchregregdData($this);
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
        
        //データがあるかどうかチェック
        $query = knjo133Query::countData($this->SCHREGNO);
        $count = $db->getOne($query);
        if($count == 0){
            $this->setMessage("対象データがありません");
            return false;
        }
        
        $data = array();
        
        //データ作成
        $data["利用業務ユニット"] = "AK02";
        
        //送信先
        $query = knjo133Query::getToSchool($this->SCHREGNO, "1");
        $cnt = $db->getOne($query);
        if($cnt > 0){
            $query = knjo133Query::getToSchool($this->SCHREGNO);
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            
            if($row["INFO_FLG"] != "2"){    //教育委員会タグを作るかどうか
                if($row["ID"] != ""){
                    $data["送信先情報"]["教育委員会"]["教育委員会ID"] = $row["ID"];
                }
                $data["送信先情報"]["教育委員会"]["外字教育委員会名"] = $row["GNAME"];
                $data["送信先情報"]["教育委員会"]["内字教育委員会名"] = $row["NNAME"];
                if($row["SECTION"] != ""){
                    $data["送信先情報"]["教育委員会"]["部課係"] = $row["SECTION"];
                }
                if($row["NADDR"] != ""){
                    $data["送信先情報"]["教育委員会"]["所在地"]["lgxml:住所コード"] = $row["ADDRCD"];
                    $data["送信先情報"]["教育委員会"]["所在地"]["lgxml:外字住所"] = $row["GADDR"];
                    $data["送信先情報"]["教育委員会"]["所在地"]["lgxml:内字住所"] = $row["NADDR"];
                    $data["送信先情報"]["教育委員会"]["所在地"]["lgxml:外字方書"] = $row["GKATAGAKI"];
                    $data["送信先情報"]["教育委員会"]["所在地"]["lgxml:内字方書"] = $row["NKATAGAKI"];
                    $data["送信先情報"]["教育委員会"]["所在地"]["lgxml:郵便番号"] = $row["ZIPCD"];
                }
                if($row["TELNO"] != ""){
                    $data["送信先情報"]["教育委員会"]["電話番号"] = $row["TELNO"];
                }
                if($row["SYOKU"] != ""){
                    $data["送信先情報"]["教育委員会"]["職"] = $row["SYOKU"];
                }
            }else{
                if($row["ID"] != ""){
                    $data["送信先情報"]["学校"]["学校ID"] = $row["ID"];
                }
                $data["送信先情報"]["学校"]["外字学校名"] = $row["GNAME"];
                $data["送信先情報"]["学校"]["内字学校名"] = $row["NNAME"];
                $data["送信先情報"]["学校"]["所在地"]["lgxml:住所コード"] = $row["ADDRCD"];
                $data["送信先情報"]["学校"]["所在地"]["lgxml:外字住所"] = $row["GADDR"];
                $data["送信先情報"]["学校"]["所在地"]["lgxml:内字住所"] = $row["NADDR"];
                $data["送信先情報"]["学校"]["所在地"]["lgxml:外字方書"] = $row["GKATAGAKI"];
                $data["送信先情報"]["学校"]["所在地"]["lgxml:内字方書"] = $row["NKATAGAKI"];
                $data["送信先情報"]["学校"]["所在地"]["lgxml:郵便番号"] = $row["ZIPCD"];
                if($row["BUN_ID"] != ""){
                    $data["送信先情報"]["学校"]["分校ID"] = $row["BUN_ID"];
                }
                if($row["BUN_GNAME"] != ""){
                    $data["送信先情報"]["学校"]["外字分校名"] = $row["BUN_GNAME"];
                }
                if($row["BUN_NNAME"] != ""){
                    $data["送信先情報"]["学校"]["内字分校名"] = $row["BUN_NNAME"];
                }
                if($row["BUN_NADDR"] != ""){
                    $data["送信先情報"]["学校"]["分校所在地"]["lgxml:住所コード"] = $row["BUN_ADDRCD"];
                    $data["送信先情報"]["学校"]["分校所在地"]["lgxml:外字住所"] = $row["BUN_GADDR"];
                    $data["送信先情報"]["学校"]["分校所在地"]["lgxml:内字住所"] = $row["BUN_NADDR"];
                    $data["送信先情報"]["学校"]["分校所在地"]["lgxml:外字方書"] = $row["BUN_GKATAGAKI"];
                    $data["送信先情報"]["学校"]["分校所在地"]["lgxml:内字方書"] = $row["BUN_NKATAGAKI"];
                    $data["送信先情報"]["学校"]["分校所在地"]["lgxml:郵便番号"] = $row["BUN_ZIPCD"];
                }
                if($row["TELNO"] != ""){
                    $data["送信先情報"]["学校"]["電話番号"] = $row["TELNO"];
                }
                if($row["SYOKU"] != ""){
                    $data["送信先情報"]["学校"]["職"] = $row["SYOKU"];
                }
                if($row["BIKOU"] != ""){
                    $data["送信先情報"]["学校"]["備考"] = $row["BIKOU"];
                }
            }
        }else{
            $data["送信先情報"] = "";
        }
        
        //送信元
        $query = knjo133Query::getFromSchool($this->SCHREGNO, "1");
        $cnt = $db->getOne($query);
        if($cnt > 0){
            $query = knjo133Query::getFromSchool($this->SCHREGNO);
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            
            if($row["INFO_FLG"] != "2"){    //教育委員会タグを作るかどうか
                if($row["ID"] != ""){
                    $data["送信元情報"]["教育委員会"]["教育委員会ID"] = $row["ID"];
                }
                $data["送信元情報"]["教育委員会"]["外字教育委員会名"] = $row["GNAME"];
                $data["送信元情報"]["教育委員会"]["内字教育委員会名"] = $row["NNAME"];
                if($row["SECTION"] != ""){
                    $data["送信元情報"]["教育委員会"]["部課係"] = $row["SECTION"];
                }
                if($row["NADDR"] != ""){
                    $data["送信元情報"]["教育委員会"]["所在地"]["lgxml:住所コード"] = $row["ADDRCD"];
                    $data["送信元情報"]["教育委員会"]["所在地"]["lgxml:外字住所"] = $row["GADDR"];
                    $data["送信元情報"]["教育委員会"]["所在地"]["lgxml:内字住所"] = $row["NADDR"];
                    $data["送信元情報"]["教育委員会"]["所在地"]["lgxml:外字方書"] = $row["GKATAGAKI"];
                    $data["送信元情報"]["教育委員会"]["所在地"]["lgxml:内字方書"] = $row["NKATAGAKI"];
                    $data["送信元情報"]["教育委員会"]["所在地"]["lgxml:郵便番号"] = $row["ZIPCD"];
                }
                if($row["TELNO"] != ""){
                    $data["送信元情報"]["教育委員会"]["電話番号"] = $row["TELNO"];
                }
                if($row["SYOKU"] != ""){
                    $data["送信元情報"]["教育委員会"]["職"] = $row["SYOKU"];
                }
            }else{
                if($row["ID"] != ""){
                    $data["送信元情報"]["学校"]["学校ID"] = $row["ID"];
                }
                $data["送信元情報"]["学校"]["外字学校名"] = $row["GNAME"];
                $data["送信元情報"]["学校"]["内字学校名"] = $row["NNAME"];
                $data["送信元情報"]["学校"]["所在地"]["lgxml:住所コード"] = $row["ADDRCD"];
                $data["送信元情報"]["学校"]["所在地"]["lgxml:外字住所"] = $row["GADDR"];
                $data["送信元情報"]["学校"]["所在地"]["lgxml:内字住所"] = $row["NADDR"];
                $data["送信元情報"]["学校"]["所在地"]["lgxml:外字方書"] = $row["GKATAGAKI"];
                $data["送信元情報"]["学校"]["所在地"]["lgxml:内字方書"] = $row["NKATAGAKI"];
                $data["送信元情報"]["学校"]["所在地"]["lgxml:郵便番号"] = $row["ZIPCD"];
                if($row["BUN_ID"] != ""){
                    $data["送信元情報"]["学校"]["分校ID"] = $row["BUN_ID"];
                }
                if($row["BUN_GNAME"] != ""){
                    $data["送信元情報"]["学校"]["外字分校名"] = $row["BUN_GNAME"];
                }
                if($row["BUN_NNAME"] != ""){
                    $data["送信元情報"]["学校"]["内字分校名"] = $row["BUN_NNAME"];
                }
                if($row["BUN_NADDR"] != ""){
                    $data["送信元情報"]["学校"]["分校所在地"]["lgxml:住所コード"] = $row["BUN_ADDRCD"];
                    $data["送信元情報"]["学校"]["分校所在地"]["lgxml:外字住所"] = $row["BUN_GADDR"];
                    $data["送信元情報"]["学校"]["分校所在地"]["lgxml:内字住所"] = $row["BUN_NADDR"];
                    $data["送信元情報"]["学校"]["分校所在地"]["lgxml:外字方書"] = $row["BUN_GKATAGAKI"];
                    $data["送信元情報"]["学校"]["分校所在地"]["lgxml:内字方書"] = $row["BUN_NKATAGAKI"];
                    $data["送信元情報"]["学校"]["分校所在地"]["lgxml:郵便番号"] = $row["BUN_ZIPCD"];
                }
                if($row["TELNO"] != ""){
                    $data["送信元情報"]["学校"]["電話番号"] = $row["TELNO"];
                }
                if($row["SYOKU"] != ""){
                    $data["送信元情報"]["学校"]["職"] = $row["SYOKU"];
                }
                if($row["BIKOU"] != ""){
                    $data["送信元情報"]["学校"]["備考"] = $row["BIKOU"];
                }
            }
        }else{
            $data["送信元情報"] = "";
        }
        
        //児童生徒健康診断票情報(CE_APP_HOKEN_S_MST)
        $query = knjo133Query::getHokenSMst($this->SCHREGNO);
        $hokenResult = $db->query($query);
        
        while($row = $hokenResult->fetchRow(DB_FETCHMODE_ASSOC)){
            $data["児童生徒健康診断票情報"]["健康診断種別"] = $row["SYUBETU"];
            
            //児童生徒健康診断票情報＞児童生徒＞基本情報
            if($row["STUDENT_ID"] != ""){
                $data["児童生徒健康診断票情報"]["児童生徒"]["基本情報"]["児童生徒ID"] = $row["STUDENT_ID"];
            }
            $data["児童生徒健康診断票情報"]["児童生徒"]["基本情報"]["氏名"]["lgxml:外字氏名"] = $row["STUDENT_GNAME"];
            $data["児童生徒健康診断票情報"]["児童生徒"]["基本情報"]["氏名"]["lgxml:内字氏名"] = $row["STUDENT_NNAME"];
            $data["児童生徒健康診断票情報"]["児童生徒"]["基本情報"]["氏名"]["lgxml:フリガナ"] = $row["STUDENT_KANA"];
            
            //通称名
            $query = knjo133Query::getAlias($this->SCHREGNO, "1");
            $aliasCnt = $db->getOne($query);
            
            if($aliasCnt > 0){
                $query = knjo133Query::getAlias($this->SCHREGNO);
                $result = $db->query($query);
                
                $cnt = 0;
                
                while($aliasRow = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                    $data["児童生徒健康診断票情報"]["児童生徒"]["基本情報"]["通称名"][$cnt]["lgxml:外字氏名"] = $aliasRow["GNAME"];
                    $data["児童生徒健康診断票情報"]["児童生徒"]["基本情報"]["通称名"][$cnt]["lgxml:内字氏名"] = $aliasRow["NNAME"];
                    $data["児童生徒健康診断票情報"]["児童生徒"]["基本情報"]["通称名"][$cnt]["lgxml:フリガナ"] = $aliasRow["NAME_KANA"];
                    
                    $cnt++;
                }
            }
            
            //性別・生年月日・現住所
            $data["児童生徒健康診断票情報"]["児童生徒"]["基本情報"]["性別"] = $row["SEX"];
            $data["児童生徒健康診断票情報"]["児童生徒"]["基本情報"]["生年月日"] = $row["BIRTHDAY"];
            if($row["STUDENT_NADDR"] != "" || $row["STUDENT_BIKOU"] != ""){
                $data["児童生徒健康診断票情報"]["児童生徒"]["基本情報"]["現住所"]["住所"]["lgxml:住所コード"] = $row["STUDENT_ADDRCD"];
                $data["児童生徒健康診断票情報"]["児童生徒"]["基本情報"]["現住所"]["住所"]["lgxml:外字住所"] = $row["STUDENT_GADDR"];
                $data["児童生徒健康診断票情報"]["児童生徒"]["基本情報"]["現住所"]["住所"]["lgxml:内字住所"] = $row["STUDENT_NADDR"];
                $data["児童生徒健康診断票情報"]["児童生徒"]["基本情報"]["現住所"]["住所"]["lgxml:外字方書"] = $row["STUDENT_GKATAGAKI"];
                $data["児童生徒健康診断票情報"]["児童生徒"]["基本情報"]["現住所"]["住所"]["lgxml:内字方書"] = $row["STUDENT_NKATAGAKI"];
                $data["児童生徒健康診断票情報"]["児童生徒"]["基本情報"]["現住所"]["住所"]["lgxml:郵便番号"] = $row["STUDENT_ZIPCD"];
                if($row["STUDENT_BIKOU"] != ""){
                    $data["児童生徒健康診断票情報"]["児童生徒"]["基本情報"]["現住所"]["備考"] = $row["STUDENT_BIKOU"];
                }
            }
            
            //現住所_その他
            $query = knjo133Query::getStAdd($this->SCHREGNO, "1");
            $addCnt = $db->getOne($query);
            
            if($addCnt > 0){
                $query = knjo133Query::getStAdd($this->SCHREGNO);
                $result = $db->query($query);
                
                $cnt = 0;
                while($addRow = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                    $data["児童生徒健康診断票情報"]["児童生徒"]["基本情報"]["現住所_その他"][$cnt]["住所"]["lgxml:住所コード"] = $addRow["ADDRCD"];
                    $data["児童生徒健康診断票情報"]["児童生徒"]["基本情報"]["現住所_その他"][$cnt]["住所"]["lgxml:外字住所"] = $addRow["GADDR"];
                    $data["児童生徒健康診断票情報"]["児童生徒"]["基本情報"]["現住所_その他"][$cnt]["住所"]["lgxml:内字住所"] = $addRow["NADDR"];
                    $data["児童生徒健康診断票情報"]["児童生徒"]["基本情報"]["現住所_その他"][$cnt]["住所"]["lgxml:外字方書"] = $addRow["GKATAGAKI"];
                    $data["児童生徒健康診断票情報"]["児童生徒"]["基本情報"]["現住所_その他"][$cnt]["住所"]["lgxml:内字方書"] = $addRow["NKATAGAKI"];
                    $data["児童生徒健康診断票情報"]["児童生徒"]["基本情報"]["現住所_その他"][$cnt]["住所"]["lgxml:郵便番号"] = $addRow["ZIPCD"];
                    if($addRow["BIKOU"] != ""){
                        $data["児童生徒健康診断票情報"]["児童生徒"]["基本情報"]["現住所_その他"][$cnt]["備考"] = $addRow["BIKOU"];
                    }
                    
                    $cnt++;
                }
            }
        }
        //児童生徒健康診断票情報＞児童生徒＞在学学年
        $query = knjo133Query::getZaigaku($this->SCHREGNO);
        $result = $db->query($query);
        
        $cnt = 0;
        while($zaiRow = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $data["児童生徒健康診断票情報"]["児童生徒"]["在学学年"][$cnt]["学年"] = $zaiRow["GRADE"];
            $data["児童生徒健康診断票情報"]["児童生徒"]["在学学年"][$cnt]["学級"] = $zaiRow["HR_CLASS"];
            $data["児童生徒健康診断票情報"]["児童生徒"]["在学学年"][$cnt]["番号"] = $zaiRow["ATTENDNO"];
            
            $cnt++;
        }
        
        //児童生徒健康診断票情報＞児童生徒＞小学校
        $query = knjo133Query::getHokenPMst($this->SCHREGNO, "1");
        $pCnt = $db->getOne($query);
        if($pCnt > 0){
            $query = knjo133Query::getHokenPMst($this->SCHREGNO);
            $result = $db->query($query);
            
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                if($row["ID"] != ""){
                    $data["児童生徒健康診断票情報"]["児童生徒"]["小学校"]["学校ID"] = $row["ID"];
                }
                $data["児童生徒健康診断票情報"]["児童生徒"]["小学校"]["外字学校名"] = $row["GNAME"];
                $data["児童生徒健康診断票情報"]["児童生徒"]["小学校"]["内字学校名"] = $row["NNAME"];
                $data["児童生徒健康診断票情報"]["児童生徒"]["小学校"]["所在地"]["lgxml:住所コード"] = $row["ADDRCD"];
                $data["児童生徒健康診断票情報"]["児童生徒"]["小学校"]["所在地"]["lgxml:外字住所"] = $row["GADDR"];
                $data["児童生徒健康診断票情報"]["児童生徒"]["小学校"]["所在地"]["lgxml:内字住所"] = $row["NADDR"];
                $data["児童生徒健康診断票情報"]["児童生徒"]["小学校"]["所在地"]["lgxml:外字方書"] = $row["GKATAGAKI"];
                $data["児童生徒健康診断票情報"]["児童生徒"]["小学校"]["所在地"]["lgxml:内字方書"] = $row["NKATAGAKI"];
                $data["児童生徒健康診断票情報"]["児童生徒"]["小学校"]["所在地"]["lgxml:郵便番号"] = $row["ZIPCD"];
                if($row["BUN_ID"] != ""){
                    $data["児童生徒健康診断票情報"]["児童生徒"]["小学校"]["分校ID"] = $row["BUN_ID"];
                }
                if($row["BUN_GNAME"] != ""){
                    $data["児童生徒健康診断票情報"]["児童生徒"]["小学校"]["外字分校名"] = $row["BUN_GNAME"];
                }
                if($row["BUN_NNAME"] != ""){
                    $data["児童生徒健康診断票情報"]["児童生徒"]["小学校"]["内字分校名"] = $row["BUN_NNAME"];
                }
                if($row["BUN_NADDR"] != ""){
                    $data["児童生徒健康診断票情報"]["児童生徒"]["小学校"]["分校所在地"]["lgxml:住所コード"] = $row["BUN_ADDRCD"];
                    $data["児童生徒健康診断票情報"]["児童生徒"]["小学校"]["分校所在地"]["lgxml:外字住所"] = $row["BUN_GADDR"];
                    $data["児童生徒健康診断票情報"]["児童生徒"]["小学校"]["分校所在地"]["lgxml:内字住所"] = $row["BUN_NADDR"];
                    $data["児童生徒健康診断票情報"]["児童生徒"]["小学校"]["分校所在地"]["lgxml:外字方書"] = $row["BUN_GKATAGAKI"];
                    $data["児童生徒健康診断票情報"]["児童生徒"]["小学校"]["分校所在地"]["lgxml:内字方書"] = $row["BUN_NKATAGAKI"];
                    $data["児童生徒健康診断票情報"]["児童生徒"]["小学校"]["分校所在地"]["lgxml:郵便番号"] = $row["BUN_ZIPCD"];
                }
                if($row["TELNO"] != ""){
                    $data["児童生徒健康診断票情報"]["児童生徒"]["小学校"]["電話番号"] = $row["TELNO"];
                }
                if($row["SYOKU"] != ""){
                    $data["児童生徒健康診断票情報"]["児童生徒"]["小学校"]["職"] = $row["SYOKU"];
                }
                if($row["BIKOU"] != ""){
                    $data["児童生徒健康診断票情報"]["児童生徒"]["小学校"]["備考"] = $row["BIKOU"];
                }
            }
        }
            
        //児童生徒健康診断票情報＞児童生徒＞中学校
        $query = knjo133Query::getHokenJMst($this->SCHREGNO, "1");
        $jCnt = $db->getOne($query);
        if($jCnt > 0){
            $query = knjo133Query::getHokenJMst($this->SCHREGNO);
            $result = $db->query($query);
            
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                if($row["ID"] != ""){
                    $data["児童生徒健康診断票情報"]["児童生徒"]["中学校"]["学校ID"] = $row["ID"];
                }
                $data["児童生徒健康診断票情報"]["児童生徒"]["中学校"]["外字学校名"] = $row["GNAME"];
                $data["児童生徒健康診断票情報"]["児童生徒"]["中学校"]["内字学校名"] = $row["NNAME"];
                $data["児童生徒健康診断票情報"]["児童生徒"]["中学校"]["所在地"]["lgxml:住所コード"] = $row["ADDRCD"];
                $data["児童生徒健康診断票情報"]["児童生徒"]["中学校"]["所在地"]["lgxml:外字住所"] = $row["GADDR"];
                $data["児童生徒健康診断票情報"]["児童生徒"]["中学校"]["所在地"]["lgxml:内字住所"] = $row["NADDR"];
                $data["児童生徒健康診断票情報"]["児童生徒"]["中学校"]["所在地"]["lgxml:外字方書"] = $row["GKATAGAKI"];
                $data["児童生徒健康診断票情報"]["児童生徒"]["中学校"]["所在地"]["lgxml:内字方書"] = $row["NKATAGAKI"];
                $data["児童生徒健康診断票情報"]["児童生徒"]["中学校"]["所在地"]["lgxml:郵便番号"] = $row["ZIPCD"];
                if($row["BUN_ID"] != ""){
                    $data["児童生徒健康診断票情報"]["児童生徒"]["中学校"]["分校ID"] = $row["BUN_ID"];
                }
                if($row["BUN_GNAME"] != ""){
                    $data["児童生徒健康診断票情報"]["児童生徒"]["中学校"]["外字分校名"] = $row["BUN_GNAME"];
                }
                if($row["BUN_NNAME"] != ""){
                    $data["児童生徒健康診断票情報"]["児童生徒"]["中学校"]["内字分校名"] = $row["BUN_NNAME"];
                }
                if($row["BUN_NADDR"] != ""){
                    $data["児童生徒健康診断票情報"]["児童生徒"]["中学校"]["分校所在地"]["lgxml:住所コード"] = $row["BUN_ADDRCD"];
                    $data["児童生徒健康診断票情報"]["児童生徒"]["中学校"]["分校所在地"]["lgxml:外字住所"] = $row["BUN_GADDR"];
                    $data["児童生徒健康診断票情報"]["児童生徒"]["中学校"]["分校所在地"]["lgxml:内字住所"] = $row["BUN_NADDR"];
                    $data["児童生徒健康診断票情報"]["児童生徒"]["中学校"]["分校所在地"]["lgxml:外字方書"] = $row["BUN_GKATAGAKI"];
                    $data["児童生徒健康診断票情報"]["児童生徒"]["中学校"]["分校所在地"]["lgxml:内字方書"] = $row["BUN_NKATAGAKI"];
                    $data["児童生徒健康診断票情報"]["児童生徒"]["中学校"]["分校所在地"]["lgxml:郵便番号"] = $row["BUN_ZIPCD"];
                }
                if($row["TELNO"] != ""){
                    $data["児童生徒健康診断票情報"]["児童生徒"]["中学校"]["電話番号"] = $row["TELNO"];
                }
                if($row["SYOKU"] != ""){
                    $data["児童生徒健康診断票情報"]["児童生徒"]["中学校"]["職"] = $row["SYOKU"];
                }
                if($row["BIKOU"] != ""){
                    $data["児童生徒健康診断票情報"]["児童生徒"]["中学校"]["備考"] = $row["BIKOU"];
                }
            }
        }
            
        
        //児童生徒健康診断票情報＞健康診断一般
        $query = knjo133Query::getMedical($this->SCHREGNO, "1");
        $medCnt = $db->getOne($query);
        if($medCnt > 0){
            $query = knjo133Query::getMedical($this->SCHREGNO);
            $result = $db->query($query);
            $cnt = 0;
            
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $data["児童生徒健康診断票情報"]["健康診断一般"][$cnt]["年度"] = $row["YEAR"];
                $data["児童生徒健康診断票情報"]["健康診断一般"][$cnt]["年齢"] = $row["AGE"];
                
                //発育測定
                if($row["HEIGHT"] != "" || $row["WEIGHT"] != "" || $row["SIT_HEIGHT"] != ""){
                    $data["児童生徒健康診断票情報"]["健康診断一般"][$cnt]["発育測定"]["身長"] = $row["HEIGHT"];
                    $data["児童生徒健康診断票情報"]["健康診断一般"][$cnt]["発育測定"]["体重"] = $row["WEIGHT"];
                    if($row["SIT_HEIGHT"] != ""){
                        $data["児童生徒健康診断票情報"]["健康診断一般"][$cnt]["発育測定"]["座高"] = $row["SIT_HEIGHT"];
                    }
                }
                
                //栄養状態
                if($row["NUT_DISORDER"] != ""){
                    $data["児童生徒健康診断票情報"]["健康診断一般"][$cnt]["栄養状態"]["異常の有無"] = $row["NUT_DISORDER"];
                    if($row["NUT_DISORDER"] == "1"){
                        if($row["NUT_DISEASE_GNAME"] != ""){
                            $data["児童生徒健康診断票情報"]["健康診断一般"][$cnt]["栄養状態"]["外字疾病名"] = $row["NUT_DISEASE_GNAME"];
                        }
                        if($row["NUT_DISEASE_NNAME"] != ""){
                            $data["児童生徒健康診断票情報"]["健康診断一般"][$cnt]["栄養状態"]["内字疾病名"] = $row["NUT_DISEASE_NNAME"];
                        }
                    }
                }
                
                //脊柱胸郭四肢
                if($row["SCL_DISORDER"] != "" || $row["SPINE_DISORDER"] != "" || $row["CHEST_DISORDER"] != "" || $row["LIMB_DISORDER"] != ""){
                    $data["児童生徒健康診断票情報"]["健康診断一般"][$cnt]["脊柱胸郭四肢"]["疾病及び異常"]["異常の有無"] = $row["SCL_DISORDER"];
                    if($row["SCL_DISORDER"] == "1"){
                        if($row["SCL_DISEASE_GNAME"] != ""){
                            $data["児童生徒健康診断票情報"]["健康診断一般"][$cnt]["脊柱胸郭四肢"]["疾病及び異常"]["外字疾病名"] = $row["SCL_DISEASE_GNAME"];
                        }
                        if($row["SCL_DISEASE_NNAME"] != ""){
                            $data["児童生徒健康診断票情報"]["健康診断一般"][$cnt]["脊柱胸郭四肢"]["疾病及び異常"]["内字疾病名"] = $row["SCL_DISEASE_NNAME"];
                        }
                    }
                }
                if($row["SPINE_DISORDER"] != ""){
                    $data["児童生徒健康診断票情報"]["健康診断一般"][$cnt]["脊柱胸郭四肢"]["脊柱"]["異常の有無"] = $row["SPINE_DISORDER"];
                    if($row["SPINE_DISORDER"] == "1"){
                        if($row["SPINE_DISEASE_GNAME"] != ""){
                            $data["児童生徒健康診断票情報"]["健康診断一般"][$cnt]["脊柱胸郭四肢"]["脊柱"]["外字疾病名"] = $row["SPINE_DISEASE_GNAME"];
                        }
                        if($row["SPINE_DISEASE_NNAME"] != ""){
                            $data["児童生徒健康診断票情報"]["健康診断一般"][$cnt]["脊柱胸郭四肢"]["脊柱"]["内字疾病名"] = $row["SPINE_DISEASE_NNAME"];
                        }
                    }
                }
                if($row["CHEST_DISORDER"] != ""){
                    $data["児童生徒健康診断票情報"]["健康診断一般"][$cnt]["脊柱胸郭四肢"]["胸郭"]["異常の有無"] = $row["CHEST_DISORDER"];
                    if($row["CHEST_DISORDER"] == "1"){
                        if($row["CHEST_DISEASE_GNAME"] != ""){
                            $data["児童生徒健康診断票情報"]["健康診断一般"][$cnt]["脊柱胸郭四肢"]["胸郭"]["外字疾病名"] = $row["CHEST_DISEASE_GNAME"];
                        }
                        if($row["CHEST_DISEASE_NNAME"] != ""){
                            $data["児童生徒健康診断票情報"]["健康診断一般"][$cnt]["脊柱胸郭四肢"]["胸郭"]["内字疾病名"] = $row["CHEST_DISEASE_NNAME"];
                        }
                    }
                }
                if($row["LIMB_DISORDER"] != ""){
                    $data["児童生徒健康診断票情報"]["健康診断一般"][$cnt]["脊柱胸郭四肢"]["四肢"]["異常の有無"] = $row["LIMB_DISORDER"];
                    if($row["LIMB_DISORDER"] == "1"){
                        if($row["LIMB_DISEASE_GNAME"] != ""){
                            $data["児童生徒健康診断票情報"]["健康診断一般"][$cnt]["脊柱胸郭四肢"]["四肢"]["外字疾病名"] = $row["LIMB_DISEASE_GNAME"];
                        }
                        if($row["LIMB_DISEASE_NNAME"] != ""){
                            $data["児童生徒健康診断票情報"]["健康診断一般"][$cnt]["脊柱胸郭四肢"]["四肢"]["内字疾病名"] = $row["LIMB_DISEASE_NNAME"];
                        }
                    }
                }
                
                //視力
                if($row["N_EYESIGHT_RIGHT"] != "" || $row["N_EYESIGHT_LEFT"] != ""){
                    $data["児童生徒健康診断票情報"]["健康診断一般"][$cnt]["視力"]["裸眼"]["右"] = $row["N_EYESIGHT_RIGHT"];
                    $data["児童生徒健康診断票情報"]["健康診断一般"][$cnt]["視力"]["裸眼"]["左"] = $row["N_EYESIGHT_LEFT"];
                }
                if($row["C_EYESIGHT_RIGHT"] != "" || $row["C_EYESIGHT_LEFT"] != ""){
                    $data["児童生徒健康診断票情報"]["健康診断一般"][$cnt]["視力"]["矯正"]["右"] = $row["C_EYESIGHT_RIGHT"];
                    $data["児童生徒健康診断票情報"]["健康診断一般"][$cnt]["視力"]["矯正"]["左"] = $row["C_EYESIGHT_LEFT"];
                }
                
                //眼の疾病及び異常
                if($row["EYE_DISORDER"] != ""){
                    $data["児童生徒健康診断票情報"]["健康診断一般"][$cnt]["眼の疾病及び異常"]["異常の有無"] = $row["EYE_DISORDER"];
                    if($row["EYE_DISORDER"] == "1"){
                        if($row["EYE_DISEASE_GNAME"] != ""){
                            $data["児童生徒健康診断票情報"]["健康診断一般"][$cnt]["眼の疾病及び異常"]["外字疾病名"] = $row["EYE_DISEASE_GNAME"];
                        }
                        if($row["EYE_DISEASE_NNAME"] != ""){
                            $data["児童生徒健康診断票情報"]["健康診断一般"][$cnt]["眼の疾病及び異常"]["内字疾病名"] = $row["EYE_DISEASE_NNAME"];
                        }
                    }
                }
                
                //聴力
                if($row["HEAR_RESULT_RIGHT"] != "" || $row["HEAR_LEVEL_RIGHT"] != "" || $row["HEAR_RESULT_LEFT"] != "" || $row["HEAR_LEVEL_LEFT"] != ""){
                    $data["児童生徒健康診断票情報"]["健康診断一般"][$cnt]["聴力"]["右"]["結果"] = $row["HEAR_RESULT_RIGHT"];
                    if($row["HEAR_LEVEL_RIGHT"] != ""){
                        $data["児童生徒健康診断票情報"]["健康診断一般"][$cnt]["聴力"]["右"]["聴力レベル"] = $row["HEAR_LEVEL_RIGHT"];
                    }
                    $data["児童生徒健康診断票情報"]["健康診断一般"][$cnt]["聴力"]["左"]["結果"] = $row["HEAR_RESULT_LEFT"];
                    if($row["HEAR_LEVEL_LEFT"] != ""){
                        $data["児童生徒健康診断票情報"]["健康診断一般"][$cnt]["聴力"]["左"]["聴力レベル"] = $row["HEAR_LEVEL_LEFT"];
                    }
                }
                
                //耳鼻咽頭疾患
                if($row["ENT_DISORDER"] != "" || $row["EAR_DISORDER"] != "" || $row["NOSE_DISORDER"] != "" || $row["THROAT_DISORDER"] != ""){
                    $data["児童生徒健康診断票情報"]["健康診断一般"][$cnt]["耳鼻咽頭疾患"]["疾病及び異常"]["異常の有無"] = $row["ENT_DISORDER"];
                    if($row["ENT_DISORDER"] == "1"){
                        if($row["ENT_DISEASE_GNAME"] != ""){
                            $data["児童生徒健康診断票情報"]["健康診断一般"][$cnt]["耳鼻咽頭疾患"]["疾病及び異常"]["外字疾病名"] = $row["ENT_DISEASE_GNAME"];
                        }
                        if($row["ENT_DISEASE_NNAME"] != ""){
                            $data["児童生徒健康診断票情報"]["健康診断一般"][$cnt]["耳鼻咽頭疾患"]["疾病及び異常"]["内字疾病名"] = $row["ENT_DISEASE_NNAME"];
                        }
                    }
                }
                if($row["EAR_DISORDER"] != ""){
                    $data["児童生徒健康診断票情報"]["健康診断一般"][$cnt]["耳鼻咽頭疾患"]["耳"]["異常の有無"] = $row["EAR_DISORDER"];
                    if($row["EAR_DISORDER"] == "1"){
                        if($row["EAR_DISEASE_GNAME"] != ""){
                            $data["児童生徒健康診断票情報"]["健康診断一般"][$cnt]["耳鼻咽頭疾患"]["耳"]["外字疾病名"] = $row["EAR_DISEASE_GNAME"];
                        }
                        if($row["EAR_DISEASE_NNAME"] != ""){
                            $data["児童生徒健康診断票情報"]["健康診断一般"][$cnt]["耳鼻咽頭疾患"]["耳"]["内字疾病名"] = $row["EAR_DISEASE_NNAME"];
                        }
                    }
                }
                if($row["NOSE_DISORDER"] != ""){
                    $data["児童生徒健康診断票情報"]["健康診断一般"][$cnt]["耳鼻咽頭疾患"]["鼻"]["異常の有無"] = $row["NOSE_DISORDER"];
                    if($row["NOSE_DISORDER"] == "1"){
                        if($row["NOSE_DISEASE_GNAME"] != ""){
                            $data["児童生徒健康診断票情報"]["健康診断一般"][$cnt]["耳鼻咽頭疾患"]["鼻"]["外字疾病名"] = $row["NOSE_DISEASE_GNAME"];
                        }
                        if($row["NOSE_DISEASE_NNAME"] != ""){
                            $data["児童生徒健康診断票情報"]["健康診断一般"][$cnt]["耳鼻咽頭疾患"]["鼻"]["内字疾病名"] = $row["NOSE_DISEASE_NNAME"];
                        }
                    }
                }
                if($row["THROAT_DISORDER"] != ""){
                    $data["児童生徒健康診断票情報"]["健康診断一般"][$cnt]["耳鼻咽頭疾患"]["咽頭"]["異常の有無"] = $row["THROAT_DISORDER"];
                    if($row["THROAT_DISORDER"] == "1"){
                        if($row["THROAT_DISEASE_GNAME"] != ""){
                            $data["児童生徒健康診断票情報"]["健康診断一般"][$cnt]["耳鼻咽頭疾患"]["咽頭"]["外字疾病名"] = $row["THROAT_DISEASE_GNAME"];
                        }
                        if($row["THROAT_DISEASE_NNAME"] != ""){
                            $data["児童生徒健康診断票情報"]["健康診断一般"][$cnt]["耳鼻咽頭疾患"]["咽頭"]["内字疾病名"] = $row["THROAT_DISEASE_NNAME"];
                        }
                    }
                }
                
                //皮膚疾患
                if($row["SKIN_DISORDER"] != ""){
                    $data["児童生徒健康診断票情報"]["健康診断一般"][$cnt]["皮膚疾患"]["異常の有無"] = $row["SKIN_DISORDER"];
                    if($row["SKIN_DISORDER"] == "1"){
                        if($row["SKIN_DISEASE_GNAME"] != ""){
                            $data["児童生徒健康診断票情報"]["健康診断一般"][$cnt]["皮膚疾患"]["外字疾病名"] = $row["SKIN_DISEASE_GNAME"];
                        }
                        if($row["SKIN_DISEASE_NNAME"] != ""){
                            $data["児童生徒健康診断票情報"]["健康診断一般"][$cnt]["皮膚疾患"]["内字疾病名"] = $row["SKIN_DISEASE_NNAME"];
                        }
                    }
                }
                
                //結核
                if($row["TB_DISORDER"] != "" || $row["TB_DISEASE_GNAME"] != "" || $row["TB_DISEASE_NNAME"] != "" || $row["TB_SHIDOU_KUBUN"] != ""){
                    $data["児童生徒健康診断票情報"]["健康診断一般"][$cnt]["結核"]["疾病及び異常"]["異常の有無"] = $row["TB_DISORDER"];
                    if($row["TB_DISEASE_GNAME"] != ""){
                        $data["児童生徒健康診断票情報"]["健康診断一般"][$cnt]["結核"]["疾病及び異常"]["外字疾病名"] = $row["TB_DISEASE_GNAME"];
                    }
                    if($row["TB_DISEASE_NNAME"] != ""){
                        $data["児童生徒健康診断票情報"]["健康診断一般"][$cnt]["結核"]["疾病及び異常"]["内字疾病名"] = $row["TB_DISEASE_NNAME"];
                    }
                    $data["児童生徒健康診断票情報"]["健康診断一般"][$cnt]["結核"]["指導区分"] = $row["TB_SHIDOU_KUBUN"];
                }
                
                //心臓
                if($row["HEART_TEST_GNAME"] != "" || $row["HEART_TEST_NNAME"] != "" 
                    || $row["HEART_DISORDER"] != "" || $row["HEART_DISEASE_GNAME"] != "" || $row["HEART_DISEASE_NNAME"] != ""){
                    $data["児童生徒健康診断票情報"]["健康診断一般"][$cnt]["心臓"]["外字臨床医学的検査"] = $row["HEART_TEST_GNAME"];
                    $data["児童生徒健康診断票情報"]["健康診断一般"][$cnt]["心臓"]["内字臨床医学的検査"] = $row["HEART_TEST_NNAME"];
                    $data["児童生徒健康診断票情報"]["健康診断一般"][$cnt]["心臓"]["疾病及び異常"]["異常の有無"] = $row["HEART_DISORDER"];
                    if($row["HEART_DISEASE_GNAME"] != ""){
                        $data["児童生徒健康診断票情報"]["健康診断一般"][$cnt]["心臓"]["疾病及び異常"]["外字疾病名"] = $row["HEART_DISEASE_GNAME"];
                    }
                    if($row["HEART_DISEASE_NNAME"] != ""){
                        $data["児童生徒健康診断票情報"]["健康診断一般"][$cnt]["心臓"]["疾病及び異常"]["内字疾病名"] = $row["HEART_DISEASE_NNAME"];
                    }
                }
                
                //尿
                if($row["URINE_PROTEINE"] != "" || $row["URINE_SUGER"] != "" || $row["URINE_BLOOD"] != "" || $row["URINE_OTHER"] != ""){
                    $data["児童生徒健康診断票情報"]["健康診断一般"][$cnt]["尿"]["蛋白第1次"] = $row["URINE_PROTEINE"];
                    $data["児童生徒健康診断票情報"]["健康診断一般"][$cnt]["尿"]["糖第1次"] = $row["URINE_SUGER"];
                    if($row["URINE_BLOOD"] != ""){
                        $data["児童生徒健康診断票情報"]["健康診断一般"][$cnt]["尿"]["潜血第1次"] = $row["URINE_BLOOD"];
                    }
                    if($row["URINE_OTHER"] != ""){
                        $data["児童生徒健康診断票情報"]["健康診断一般"][$cnt]["尿"]["その他の検査"] = $row["URINE_OTHER"];
                    }
                }
                
                //寄生虫卵
                if($row["PARA_DISORDER"] != ""){
                    $data["児童生徒健康診断票情報"]["健康診断一般"][$cnt]["寄生虫卵"]["異常の有無"] = $row["PARA_DISORDER"];
                    if($row["PARA_DISORDER"] == "1"){
                        if($row["PARA_DISEASE_GNAME"] != ""){
                            $data["児童生徒健康診断票情報"]["健康診断一般"][$cnt]["寄生虫卵"]["外字疾病名"] = $row["PARA_DISEASE_GNAME"];
                        }
                        if($row["PARA_DISEASE_NNAME"] != ""){
                            $data["児童生徒健康診断票情報"]["健康診断一般"][$cnt]["寄生虫卵"]["内字疾病名"] = $row["PARA_DISEASE_NNAME"];
                        }
                    }
                }
                
                //その他の疾病及び異常
                if($row["OTHER_DISORDER"] != ""){
                    $data["児童生徒健康診断票情報"]["健康診断一般"][$cnt]["その他の疾病及び異常"]["異常の有無"] = $row["OTHER_DISORDER"];
                    if($row["OTHER_DISORDER"] == "1"){
                        if($row["OTHER_DISEASE_GNAME"] != ""){
                            $data["児童生徒健康診断票情報"]["健康診断一般"][$cnt]["その他の疾病及び異常"]["外字疾病名"] = $row["OTHER_DISEASE_GNAME"];
                        }
                        if($row["OTHER_DISEASE_NNAME"] != ""){
                            $data["児童生徒健康診断票情報"]["健康診断一般"][$cnt]["その他の疾病及び異常"]["内字疾病名"] = $row["OTHER_DISEASE_NNAME"];
                        }
                    }
                }
                
                //学校医
                if($row["SCH_DOCTOR_SYOKEN"] != "" || $row["SCH_DOCTOR_DATE"] != ""){
                    $data["児童生徒健康診断票情報"]["健康診断一般"][$cnt]["学校医"]["所見"] = $row["SCH_DOCTOR_SYOKEN"];
                    $data["児童生徒健康診断票情報"]["健康診断一般"][$cnt]["学校医"]["年月日"] = $row["SCH_DOCTOR_DATE"];
                }
                
                //事後措置
                if($row["AFTERCARE"] != ""){
                    $data["児童生徒健康診断票情報"]["健康診断一般"][$cnt]["事後措置"] = $row["AFTERCARE"];
                }
                //備考
                if($row["BIKOU"] != ""){
                    $data["児童生徒健康診断票情報"]["健康診断一般"][$cnt]["備考"] = $row["BIKOU"];
                }
                $cnt++;
            }
        }
        
        //児童生徒健康診断票情報＞歯科検査
        $query = knjo133Query::getDental($this->SCHREGNO, "1");
        $dentCnt = $db->getOne($query);
        if($dentCnt > 0){
            $query = knjo133Query::getDental($this->SCHREGNO);
            $result = $db->query($query);
            
            $cnt = 0;
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $data["児童生徒健康診断票情報"]["歯科検査"][$cnt]["年度"] = $row["YEAR"];
                $data["児童生徒健康診断票情報"]["歯科検査"][$cnt]["年齢"] = $row["AGE"];
                
                if($row["DENTITION_BITE"] != ""){
                    $data["児童生徒健康診断票情報"]["歯科検査"][$cnt]["歯列咬合"] = $row["DENTITION_BITE"];
                }
                if($row["CHIN_JOINT"] != ""){
                    $data["児童生徒健康診断票情報"]["歯科検査"][$cnt]["顎関節"] = $row["CHIN_JOINT"];
                }
                if($row["PLAQUE"] != ""){
                    $data["児童生徒健康診断票情報"]["歯科検査"][$cnt]["歯垢の状態"] = $row["PLAQUE"];
                }
                if($row["GUM"] != ""){
                    $data["児童生徒健康診断票情報"]["歯科検査"][$cnt]["歯肉の状態"] = $row["GUM"];
                }
                
                //乳歯のデータ部分だけ抽出
                $check = array_slice($row, 8, 60, true);
                //データが何かしら入っていることを確認したい
                $inFlg = 0;
                foreach($check as $val){
                    if($val != ""){
                        $inFlg = 1;
                        break;
                    }
                }
                
                if($inFlg != 0){
                    //右上
                    $ru = array_slice($check, 0, 15, true);
                    //右下
                    $rd = array_slice($check, 15, 15, true);
                    //左上
                    $lu = array_slice($check, 30, 15, true);
                    //左下
                    $ld = array_slice($check, 45, 15, true);
                    
                    //共通に使う配列
                    $alpha = array("A", "B", "C", "D", "E");
                    $change = array("2" => "歯の状態", "3" => "補助記号");
                    
                    //右上
                    $alCnt = 0;
                    $chCnt = 1;
                    $createFlg = 0;
                    foreach($ru as $key => $val){
                        if($chCnt > 3){
                            $alCnt++;
                            $chCnt = 1;
                            $createFlg = 0;
                        }
                        if($chCnt == 1){
                            $data["児童生徒健康診断票情報"]["歯科検査"][$cnt]["歯式乳歯"]["右上"]["乳歯".$alpha[$alCnt]]["現在歯の有無"] = $val;
                            if($val == "1"){
                                $createFlg = 1;
                            }
                        }else if($createFlg != 0){
                            if($val != ""){
                                $data["児童生徒健康診断票情報"]["歯科検査"][$cnt]["歯式乳歯"]["右上"]["乳歯".$alpha[$alCnt]][$change[$chCnt]] = $val;
                            }
                        }
                        $chCnt++;
                    }
                    //右下
                    $alCnt = 0;
                    $chCnt = 1;
                    $createFlg = 0;
                    foreach($rd as $key => $val){
                        if($chCnt > 3){
                            $alCnt++;
                            $chCnt = 1;
                            $createFlg = 0;
                        }
                        if($chCnt == 1){
                            $data["児童生徒健康診断票情報"]["歯科検査"][$cnt]["歯式乳歯"]["右下"]["乳歯".$alpha[$alCnt]]["現在歯の有無"] = $val;
                            if($val == "1"){
                                $createFlg = 1;
                            }
                        }else if($createFlg != 0){
                            if($val != ""){
                                $data["児童生徒健康診断票情報"]["歯科検査"][$cnt]["歯式乳歯"]["右下"]["乳歯".$alpha[$alCnt]][$change[$chCnt]] = $val;
                            }
                        }
                        $chCnt++;
                    }
                    //左上
                    $alCnt = 0;
                    $chCnt = 1;
                    $createFlg = 0;
                    foreach($lu as $key => $val){
                        if($chCnt > 3){
                            $alCnt++;
                            $chCnt = 1;
                            $createFlg = 0;
                        }
                        if($chCnt == 1){
                            $data["児童生徒健康診断票情報"]["歯科検査"][$cnt]["歯式乳歯"]["左上"]["乳歯".$alpha[$alCnt]]["現在歯の有無"] = $val;
                            if($val == "1"){
                                $createFlg = 1;
                            }
                        }else if($createFlg != 0){
                            if($val != ""){
                                $data["児童生徒健康診断票情報"]["歯科検査"][$cnt]["歯式乳歯"]["左上"]["乳歯".$alpha[$alCnt]][$change[$chCnt]] = $val;
                            }
                        }
                        $chCnt++;
                    }
                    //左下
                    $alCnt = 0;
                    $chCnt = 1;
                    $createFlg = 0;
                    foreach($ld as $key => $val){
                        if($chCnt > 3){
                            $alCnt++;
                            $chCnt = 1;
                            $createFlg = 0;
                        }
                        if($chCnt == 1){
                            $data["児童生徒健康診断票情報"]["歯科検査"][$cnt]["歯式乳歯"]["左下"]["乳歯".$alpha[$alCnt]]["現在歯の有無"] = $val;
                            if($val == "1"){
                                $createFlg = 1;
                            }
                        }else if($createFlg != 0){
                            if($val != ""){
                                $data["児童生徒健康診断票情報"]["歯科検査"][$cnt]["歯式乳歯"]["左下"]["乳歯".$alpha[$alCnt]][$change[$chCnt]] = $val;
                            }
                        }
                        $chCnt++;
                    }
                }
                
                //歯式永久歯
                $check = array();
                $check = array_slice($row, 68, 96, true);
                
                $inFlg = 0;
                
                foreach($check as $val){
                    if($val != ""){
                        $inFlg = 1;
                        break;
                    }
                }
                
                if($inFlg != 0){
                    //右上
                    $ru = array();
                    $ru = array_slice($check, 0, 24, true);
                    //右下
                    $rd = array();
                    $rd = array_slice($check, 24, 24, true);
                    //左上
                    $lu = array();
                    $lu = array_slice($check, 48, 24, true);
                    //左下
                    $ld = array();
                    $ld = array_slice($check, 72, 24, true);
                    
                    $change = array("2" => "歯の状態", "3" => "補助記号");
                    
                    //右上
                    $alCnt = 1;
                    $chCnt = 1;
                    $createFlg = 0;
                    foreach($ru as $key => $val){
                        if($chCnt > 3){
                            $alCnt++;
                            $chCnt = 1;
                            $createFlg = 0;
                        }
                        if($chCnt == 1){
                            $data["児童生徒健康診断票情報"]["歯科検査"][$cnt]["歯式永久歯"]["右上"]["永久歯".$alCnt]["現在歯の有無"] = $val;
                            if($val == "1"){
                                $createFlg = 1;
                            }
                        }else if($createFlg != 0){
                            if($val != ""){
                                $data["児童生徒健康診断票情報"]["歯科検査"][$cnt]["歯式永久歯"]["右上"]["永久歯".$alCnt][$change[$chCnt]] = $val;
                            }
                        }
                        $chCnt++;
                    }
                    //右下
                    $alCnt = 1;
                    $chCnt = 1;
                    $createFlg = 0;
                    foreach($rd as $key => $val){
                        if($chCnt > 3){
                            $alCnt++;
                            $chCnt = 1;
                            $createFlg = 0;
                        }
                        if($chCnt == 1){
                            $data["児童生徒健康診断票情報"]["歯科検査"][$cnt]["歯式永久歯"]["右下"]["永久歯".$alCnt]["現在歯の有無"] = $val;
                            if($val == "1"){
                                $createFlg = 1;
                            }
                        }else if($createFlg != 0){
                            if($val != ""){
                                $data["児童生徒健康診断票情報"]["歯科検査"][$cnt]["歯式永久歯"]["右下"]["永久歯".$alCnt][$change[$chCnt]] = $val;
                            }
                        }
                        $chCnt++;
                    }
                    //左上
                    $alCnt = 1;
                    $chCnt = 1;
                    $createFlg = 0;
                    foreach($lu as $key => $val){
                        if($chCnt > 3){
                            $alCnt++;
                            $chCnt = 1;
                            $createFlg = 0;
                        }
                        if($chCnt == 1){
                            $data["児童生徒健康診断票情報"]["歯科検査"][$cnt]["歯式永久歯"]["左上"]["永久歯".$alCnt]["現在歯の有無"] = $val;
                            if($val == "1"){
                                $createFlg = 1;
                            }
                        }else if($createFlg != 0){
                            if($val != ""){
                                $data["児童生徒健康診断票情報"]["歯科検査"][$cnt]["歯式永久歯"]["左上"]["永久歯".$alCnt][$change[$chCnt]] = $val;
                            }
                        }
                        $chCnt++;
                    }
                    //左下
                    $alCnt = 1;
                    $chCnt = 1;
                    $createFlg = 0;
                    foreach($ld as $key => $val){
                        if($chCnt > 3){
                            $alCnt++;
                            $chCnt = 1;
                            $createFlg = 0;
                        }
                        if($chCnt == 1){
                            $data["児童生徒健康診断票情報"]["歯科検査"][$cnt]["歯式永久歯"]["左下"]["永久歯".$alCnt]["現在歯の有無"] = $val;
                            if($val == "1"){
                                $createFlg = 1;
                            }
                        }else if($createFlg != 0){
                            if($val != ""){
                                $data["児童生徒健康診断票情報"]["歯科検査"][$cnt]["歯式永久歯"]["左下"]["永久歯".$alCnt][$change[$chCnt]] = $val;
                            }
                        }
                        $chCnt++;
                    }
                }
                
                //乳歯の本数
                if($row["BT_NOW_CNT"] != "" || $row["BT_NOTTREAT_CNT"] != "" || $row["BT_TREAT_CNT"] != ""){
                    $data["児童生徒健康診断票情報"]["歯科検査"][$cnt]["乳歯の本数"]["現在歯数"] = $row["BT_NOW_CNT"];
                    $data["児童生徒健康診断票情報"]["歯科検査"][$cnt]["乳歯の本数"]["未処置歯数"] = $row["BT_NOTTREAT_CNT"];
                    $data["児童生徒健康診断票情報"]["歯科検査"][$cnt]["乳歯の本数"]["処置歯数"] = $row["BT_TREAT_CNT"];
                }
                
                //永久歯の本数
                if($row["PT_NOW_CNT"] != "" || $row["PT_NOTTREAT_CNT"] != "" || $row["PT_TREAT_CNT"] != "" || $row["PT_LOST_CNT"] != ""){
                    $data["児童生徒健康診断票情報"]["歯科検査"][$cnt]["永久歯の本数"]["現在歯数"] = $row["PT_NOW_CNT"];
                    $data["児童生徒健康診断票情報"]["歯科検査"][$cnt]["永久歯の本数"]["未処置歯数"] = $row["PT_NOTTREAT_CNT"];
                    $data["児童生徒健康診断票情報"]["歯科検査"][$cnt]["永久歯の本数"]["処置歯数"] = $row["PT_TREAT_CNT"];
                    $data["児童生徒健康診断票情報"]["歯科検査"][$cnt]["永久歯の本数"]["喪失歯数"] = $row["PT_LOST_CNT"];
                }
                //その他の疾病及び異常
                if($row["OTHER_DISORDER"] != ""){
                    $data["児童生徒健康診断票情報"]["歯科検査"][$cnt]["その他の疾病及び異常"]["異常の有無"] = $row["OTHER_DISORDER"];
                    if($row["OTHER_DISORDER"] == "1"){
                        if($row["OTHER_DISEASE_GNAME"] != ""){
                            $data["児童生徒健康診断票情報"]["歯科検査"][$cnt]["その他の疾病及び異常"]["外字疾病名"] = $row["OTHER_DISEASE_GNAME"];
                        }
                        if($row["OTHER_DISEASE_NNAME"] != ""){
                            $data["児童生徒健康診断票情報"]["歯科検査"][$cnt]["その他の疾病及び異常"]["内字疾病名"] = $row["OTHER_DISEASE_NNAME"];
                        }
                    }
                }
                //学校歯科医
                if($row["SCH_DENTIST_SYOKEN"] != "" || $row["SCH_DENTIST_DATE"] != ""){
                    $data["児童生徒健康診断票情報"]["歯科検査"][$cnt]["学校歯科医"]["所見"] = $row["SCH_DENTIST_SYOKEN"];
                    $data["児童生徒健康診断票情報"]["歯科検査"][$cnt]["学校歯科医"]["年月日"] = $row["SCH_DENTIST_DATE"];
                }
                //事後措置
                if($row["AFTERCARE"] != ""){
                    $data["児童生徒健康診断票情報"]["歯科検査"][$cnt]["事後措置"] = $row["AFTERCARE"];
                }
                
                $cnt++;
            }
        }
        
        //送信日時
        /*$query = knjo133Query::getSendDate($this->SCHREGNO);
        $send = $db->getOne($query);
        $sendex = explode(".", $send);
        
        $senddate = str_replace(" ", "T", $sendex[0]);
        $data["送信日時"] = $senddate."+09:00";*/
        $data["送信日時"] = date("c");

        
        //ファイル用に学校名取得
        if ($jCnt >0){
            $schoolKind = 'J';
        } else if($pCnt > 0) {
            $schoolKind ='P';
        }

        $query =  knjo133Query::getSchoolName($schoolKind);
        $this->schoolName = $db->getOne($query);

        //DB切断
        Query::dbCheckIn($db);


        //初期値
        $count = 0;
        $past = "message";
        
        //functionに送る
        knjo133Model::create($data, $count, $past, $this);

        
        /*new dBug($this->new);
        exit;*/

        //タイトル部分
        $elementName = "児童生徒健康診断票情報メッセージ";
        $attribute = array("xmlns" => "urn:applic:xmlns:pf:edu:schema:2020-01",
                           "xmlns:xsi" => "http://www.w3.org/2001/XMLSchema-instance",
                           "xmlns:lgxml" => "urn:applic:xmlns:pf:lgxml:schema:2020-01",
                           "xsi:schemaLocation" => "urn:applic:xmlns:pf:edu:schema:2020-01 eduAK0201s-2020-01.xsd");


        //出力
        $today = date("Ymd");
        $time = date("His");
        $fileName = $this->schoolName."_".$this->SCHREGNO."_HT".$today.$time.'.xml';  //  ファイル名
         
        $this->dom = new DomDocument('1.0');  // DOMを作成
        $this->dom->encoding = "UTF-8"; // 文字コードをUTF-8に
        $this->dom->formatOutput = true; // 出力XMLを整形(改行,タブ)する
        
        
        $this->message = $this->dom->appendChild($this->dom->createElement($elementName));
        foreach($attribute as $key => $val){
            $this->message->setAttribute($key, $val);
        }
        

        for($count = 1; $count<get_count($this->new)+1; $count++){
            knjo133Model::makeXML($this->new[$count], $this); 
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
                knjo133Model::create($val, $count, $now, $data);
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
