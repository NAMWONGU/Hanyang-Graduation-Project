### 1. DispatcherServlet : init()
Servlet 생명주기의 init()함수에서 부터 시작함.
```
@Override
	public void init() throws ServletException {
		//SimpleContext를 생성, 이때 사용자가 설정한 xml 파일을 파싱하여 SimpleContext의 정보에다 넣어줌.
		simpleContext = new SimpleContext(getServletContext(), getContextConfigLocation());
		//SimpleContext Refresh
		simpleContext.refresh();
		//servlet의 리퀘스트와 프레임워크의 리퀘스트를 매핑
		handleRequestMapping();
	}
```

### 2. BeanFactory : ComponentScan()
package 내의 Controller annotation과 Component annotation을 찾고 이를 바탕으로 프레임워크에서 제어하는 객체들을 정의한 BeanDefinition들의 ConcurrentHashMap을 만든다.
```
...
    Reflections reflections = new Reflections(simpleContext.getBasePackage());
		Set<Class<?>> annotated = reflections.getTypesAnnotatedWith(Controller.class);
		for (Class<?> clazz : annotated) {
			BeanDefinition beanDefinition = new BeanDefinition();
			beanDefinition.clazz = clazz;
			beanDefinition.type = ComponentType.CONTROLLER;
			beanDefinitionMap.put(clazz.getName(), beanDefinition);
		}
		
		annotated = reflections.getTypesAnnotatedWith(Component.class);
		for (Class<?> clazz : annotated) {
			BeanDefinition beanDefinition = new BeanDefinition();
			beanDefinition.clazz = clazz;
			beanDefinition.type = ComponentType.COMPONENT;
			beanDefinitionMap.put(clazz.getName(), beanDefinition);
		}
...
```


### 3. DispatcherServlet : handleRequestMapping()
package 내의 Controller annotation과 Component annotation을 찾고 이를 바탕으로 프레임워크에서 제어하는 객체들을 정의한 BeanDefinition들의 ConcurrentHashMap을 만든다.
```
...
    for (Class<?> clazz : getSimpleContext().getBeanFactory().getControllerBeans()) {
    //프레임워크에서 관리하는 Controller bean들을 찾는다.
			for (Method method : clazz.getDeclaredMethods()) {
			//해당 bean에 존재하는 모든 메소드를 찾는다.	
        if (method.isAnnotationPresent(RequestMapping.class)) {
				//해당 메소드에 @RequestMapping 이 존재하는지 확인한다.
          
          String url = method.getAnnotation(RequestMapping.class).value();
          //해당 Annotation에 붙어있는 url을 가져온다.
					
          if (requestHandle.get(url) != null) {
						return;
					}
					try {
						HandleMap handleMap = new HandleMap();
						...
            handleMap.handler = clazz.newInstance();
            //해당 리퀘스트요청시, 시행할 함수를 실행하기 위한 객체의 instance를 생성한다.
						requestHandle.put(url, handleMap);
					}
          ...
				}

			}
		}
...
```

### 4. DispatcherServlet : doService(HttpServletRequest req, HttpServletResponse resp)
Servlet에서 전달받은 리퀘스트를 처리한다.
```
...
  String requestUri = StringUtil.getRequestUri(req.getRequestURI(), req.getContextPath());
		//해당 리퀘스트의 URI를 뽑아내고, 그리고 프레임워크에서 처리하기 적절한 형태로 변환시켜준다.
		
		HandleMap ha = requestHandle.get(requestUri);
		//SimpleFramework를 init할 때 만들었던 requestHandle hashmap에서 해당 URI를 통해 핸들러를 가져온다.
		
		try {
			if (ha.requestMethod == RequestMethod.GET) {
				Object returnValue = ha.handleMethod.invoke(ha.handler, null);
				//핸들러를 통해서 리퀘스트에 대응하는 서버처리를 실행시켜준다.
				doResponse(req, resp, returnValue);
			} else if (ha.requestMethod == RequestMethod.POST) {
				String body = StringUtil.getBody(req);
				Object returnValue = ha.handleMethod.invoke(ha.handler, body);
				doResponse(req, resp, returnValue);
			}
			
		}
...
```
